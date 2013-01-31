package com.mediasmiths.foxtel.wf.adapter.service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIF;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCErrorNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCPassNotification;
import com.mediasmiths.foxtel.wf.adapter.model.GetQCProfileResponse;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeIntalioTXFlow;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.TCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCPassedNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCTotalFailure;
import com.mediasmiths.foxtel.wf.adapter.model.TXDeliveryFailure;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
//import com.mediasmiths.stdEvents.persistence.db.entity.EventEntity;
//import com.mediasmiths.stdEvents.persistence.rest.api.EventAPI;

public class WFAdapterRestServiceImpl implements WFAdapterRestService
{

	private static final String TC_EVENT_NAMESPACE = "http://www.foxtel.com.au/ip/tc";

	private static final String QC_EVENT_NAMESPACE = "http://www.foxtel.com.au/ip/qc";
	
	private static final String TX_EVENT_NAMESPACE = "http://www.foxtel.com.au/ip/delivery";

	private final static Logger log = Logger.getLogger(WFAdapterRestServiceImpl.class);

	@Inject
	private EventService events;

	@Inject
	private MayamClient mayamClient;
	@Inject
	private QcProfileSelector qcProfileSelector;
	@Inject
	@Named("tx.tcoutput.location")
	private String tcoutputlocation;
	@Inject
	@Named("tx.delivery.location")
	private String txDeliveryLocation;
	@Inject
	@Named("mex.marshaller")
	private Marshaller mexMarshaller;
	@Inject
	@Named("ruzz.marshaller")
	private Marshaller ruzzMarshaller;
	@Inject
	@Named("wfe.marshaller")
	private Marshaller wfeMarshaller;
	
	@Inject
	@Named("cerify.report.location")
	private String cerifyReportLocation;
	@Inject
	@Named("cerfiy.report.ardomehandle")
	private String cerifyReportArdomeserviceHandle;
	@Inject
	@Named("cerify.report.attatch")
	private boolean attachQcReports;
	
	
	
	@Override
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping()
	{
		return "ping";
	}

	@Override
	@PUT
	@Path("/material/transferforqc")
	@Produces("application/xml")
	public AssetTransferForQCResponse transferMaterialForQC(AssetTransferForQCRequest req) throws MayamClientException
	{
		//doesnt actually do a transfer! just returns location (method needs renamed)
		
		log.info("Received AssetTransferForQCRequest " + req.toString());
		final String id = req.getAssetId();
		File destinationFile;

		if (req.isForTXDelivery())
		{
			// for tx delivery we return the location of transcoded package
			String ret = tcoutputlocation + id + "/" + id + ".gxf";
			destinationFile = new File(ret);
		}
		else
		{
			String destination = mayamClient.pathToMaterial(id);
			destinationFile = new File(destination);
		}

		return new AssetTransferForQCResponse(destinationFile.getAbsolutePath());
	}

	@Override
	@GET
	@Path("/qc/profile")
	@Produces("application/xml")
	public GetQCProfileResponse getProfileForQc(
			@QueryParam("assetID") String assetID,
			@QueryParam("isForTX") boolean isForTXDelivery) throws MayamClientException
	{
		GetQCProfileResponse resp = new GetQCProfileResponse();
		String profile = qcProfileSelector.getProfileFor(assetID, isForTXDelivery);
		resp.setProfile(profile);
		return resp;
	}

	@Override
	@PUT
	@Path("/qc/autoQcFailed")
	public void notifyAutoQCFailed(AutoQCFailureNotification notification) throws MayamClientException
	{
		log.info(String.format(
				"Received notification of Auto QC failure ID %s isTX %b",
				notification.getAssetId(),
				notification.isForTXDelivery()));
		
		try
		{
			if (notification.isForTXDelivery())
			{
				// id is a package id
				saveEvent("QCProblemwithTCMedia", notification, QC_EVENT_NAMESPACE);
				mayamClient.failTaskForAsset(MayamTaskListType.TX_DELIVERY, notification.getAssetId());
			}
			else
			{
				// id is an item id
				saveEvent("AutoQCFailed", notification, QC_EVENT_NAMESPACE);
				mayamClient.autoQcFailedForMaterial(notification.getAssetId(), notification.getTaskID());
				attachQcReports(notification.getAssetId(),notification.getJobName());
				
			}
		}
		catch (MayamClientException e)
		{
			log.error("Failed to fail task!", e);
			throw e;
		}
	}

	@Override
	@PUT
	@Path("/tc/transferfortc")
	@Produces("application/xml")
	public MaterialTransferForTCResponse transferMaterialForTC(MaterialTransferForTCRequest req) throws MayamClientException
	{
		log.info("Received MaterialTransferForTCRequest " + req.toString());
		final String assetID = req.getAssetID();
		String materialID;
		
		//we dont actually need to transfer the media, just get its location
		if (req.isForTX())
		{
			materialID = mayamClient.getMaterialIDofPackageID(assetID);
		}
		else
		{
			materialID = assetID;
		}

		
		String path = mayamClient.pathToMaterial(materialID);
		
		return new MaterialTransferForTCResponse(path);
	}

	@Override
	@PUT
	@Path("/qc/autoQcPassed")
	public void notifyAutoQCPassed(AutoQCPassNotification notification) throws MayamClientException
	{
		log.info(String.format(
				"Received notification of Auto QC Pass ID %s isTX %b",
				notification.getAssetId(),
				notification.isForTXDelivery()));

		saveEvent("AutoQCPassed", notification, QC_EVENT_NAMESPACE);

		if (notification.isForTXDelivery())
		{
			// update tasks status as required, next stage will be kicked off by intalio
		}
		else
		{
			mayamClient.autoQcPassedForMaterial(notification.getAssetId(), notification.getTaskID());
			attachQcReports(notification.getAssetId(),notification.getJobName());
			
		}
	}

	private void attachQcReports(final String assetID, final String jobName) throws MayamClientException
	{
		//find report pdfs
		IOFileFilter reportPDFAcceptor = new IOFileFilter()
		{
			
			@Override
			public boolean accept(File dir, String name)
			{
				return false; //dont look in sub directories
			}
			
			@Override
			public boolean accept(File file)
			{
				String name = FilenameUtils.getName(file.getAbsolutePath());
				if(name.startsWith(jobName)){
					return true;
				}
				else{
					return false;
				}
			}
		};
		
		Collection<File> reports = FileUtils.listFiles(new File(cerifyReportLocation),reportPDFAcceptor,reportPDFAcceptor);
		//attach report(s) to item
		
		for (File file : reports)
		{
			
			if (attachQcReports)
			{
				log.info(String.format("attach file %s to material", file.getAbsolutePath()));
				mayamClient.attachFileToMaterial(assetID, file.getAbsolutePath(), cerifyReportArdomeserviceHandle);
			}
		}
	}

	@Override
	@PUT
	@Path("/qc/autoQcError")
	public void notifyAutoQCError(AutoQCErrorNotification notification) throws MayamClientException
	{
		log.info(String.format(
				"Received notification of Auto QC Error ID %s isTX %b",
				notification.getAssetId(),
				notification.isForTXDelivery()));
		try
		{
			if (notification.isForTXDelivery())
			{
				// auto qc was for tx delivery
				saveEvent("CerifyQCError", notification, QC_EVENT_NAMESPACE);
				mayamClient.failTaskForAsset(MayamTaskListType.TX_DELIVERY, notification.getAssetId());
			}
			else
			{
				// auto qc was for qc task
				saveEvent("CerifyQCError", notification, QC_EVENT_NAMESPACE);				
				mayamClient.autoQcFailedForMaterial(notification.getAssetId(),notification.getTaskID());
			}
		}
		catch (MayamClientException e)
		{
			log.error("Failed to fail task!", e);
			throw e;
		}
	}

	@Override
	@PUT
	@Path("/tc/tcFailedTotal")
	public void notifyTCFailedTotal(TCTotalFailure notification) throws MayamClientException
	{
		log.info(String.format("Received notification of TC totalfailure Package ID %s ", notification.getPackageID()));
		saveEvent("PersistentFailure", notification, TC_EVENT_NAMESPACE);

	}

	@Override
	@PUT
	@Path("/tc/tcFailed")
	public void notifyTCFailed(TCFailureNotification notification) throws MayamClientException
	{

		log.info(String.format("Received notification of TC failure Package ID %s", notification.getPackageID()));
		saveEvent("TCFailed", notification, TC_EVENT_NAMESPACE);

	}

	@Override
	@PUT
	@Path("/tc/tcPassed")
	public void notifyTCPassed(TCPassedNotification notification) throws MayamClientException
	{
		log.info(String.format("Received notification of TC passed Package ID %s", notification.getPackageID()));
		saveEvent("Transcoded", notification, TC_EVENT_NAMESPACE);

	}

	protected void saveEvent(String name, String payload, String nameSpace)
	{
		events.saveEvent(name, payload,nameSpace);
	}

	protected void saveEvent(String name, Object payload, String nameSpace)
	{
		events.saveEvent(name, payload,nameSpace);
	}

	@Override
	@GET
	@Path("/tx/autoQCRequired")
	@Produces("text/plain")
	public Boolean autoQCRequiredForTxTask(@QueryParam("taskID") Long taskID) throws MayamClientException
	{
		
		return Boolean.valueOf(mayamClient.autoQcRequiredForTXTask(taskID));
		
	}

	@Override
	@PUT
	@Path("/tx/failed")
	@Consumes("application/xml")
	public void notifyTXDeliveryFailed(TXDeliveryFailure notification)
	{
		log.fatal(String.format(
				"TX DELIVERY FAILURE FOR PACKAGE %s AT STAGE %s",
				notification.getPackageID(),
				notification.getStage()));
		
		saveEvent("DeliveryFailed", notification, TX_EVENT_NAMESPACE);
	}

	@Override
	@GET
	@Path("/tx/transcodeOutputLocation")
	@Produces("text/plain")
	public String transcodeOutputLocationForPackage(@QueryParam("packageID") String packageID)
	{
		String ret = tcoutputlocation + packageID;
		log.info(String.format("Returning transcode output location %s for package %s", ret, packageID));

		return ret;
	}

	@Override
	@GET
	@Path("/tx/deliveryLocation")
	@Produces("text/plain")
	public String deliveryLocationForPackage(@QueryParam("packageID") String packageID)
	{
		String ret = txDeliveryLocation + packageID;
		log.info(String.format("Returning delivery location %s for package %s", ret,packageID));
		
		return ret;
	}

	@Override
	@GET
	@Path("/tx/delivery/writeSegmentXML")
	@Produces("text/plain")
	public boolean writeSegmentXML(@QueryParam("packageID") String packageID) throws MayamClientException, IOException, JAXBException
	{
		log.debug(String.format("Writing segment xml for package %s",packageID));
		
		String companion;
		
		if(mayamClient.isPackageAO(packageID)){
			companion = getAOSegmentXML(packageID);
		}
		else{
			companion = getSegmentXML(packageID);	
		}
		String deliveryLocation = deliveryLocationForPackage(packageID);
		
		File deliveryLocationFile = new File(deliveryLocation);
		deliveryLocationFile.mkdirs();
		
		File segmentXmlFile = new File(String.format("%s/%s.xml", deliveryLocation, packageID));
		try
		{
			FileUtils.writeStringToFile(segmentXmlFile, companion);
			return true;
		}
		catch (IOException e)
		{
			log.error(String.format("Error writing companion xml for package %s", packageID),e);
			throw e;			
		}
		
	}

	@Override
	@GET
	@Path("/tx/delivery/getAOSegmentXML")
	@Produces("application/xml")
	public String getAOSegmentXML(@QueryParam("packageID") String packageID) throws MayamClientException, JAXBException
	{

		RuzzIF ruzzProgramme = mayamClient.getRuzzProgramme(packageID);

		StringWriter sw = new StringWriter();
		ruzzMarshaller.marshal(ruzzProgramme, sw);
		return sw.toString();

	}

	@Override
	@GET
	@Path("/tx/delivery/getSegmentXML")
	@Produces("application/xml")
	public String getSegmentXML(@QueryParam("packageID") String packageID) throws MayamClientException, JAXBException
	{

		Programme programme = mayamClient.getProgramme(packageID);

		StringWriter sw = new StringWriter();
		mexMarshaller.marshal(programme, sw);
		return sw.toString();

	}

}
