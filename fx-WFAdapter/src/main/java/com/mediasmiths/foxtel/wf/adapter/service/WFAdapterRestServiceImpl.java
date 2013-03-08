package com.mediasmiths.foxtel.wf.adapter.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIF;
import com.mediasmiths.foxtel.ip.common.events.TcFailureNotification;
import com.mediasmiths.foxtel.ip.common.events.TcNotification;
import com.mediasmiths.foxtel.ip.common.events.TcPassedNotification;
import com.mediasmiths.foxtel.ip.common.events.TcTotalFailure;
import com.mediasmiths.foxtel.ip.common.events.TxDelivered;
import com.mediasmiths.foxtel.ip.common.events.TxDeliveryFailure;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCErrorNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCPassNotification;
import com.mediasmiths.foxtel.wf.adapter.model.GetQCProfileResponse;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.TCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCPassedNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCTotalFailure;
import com.mediasmiths.foxtel.wf.adapter.model.TXDeliveryFailure;
import com.mediasmiths.foxtel.wf.adapter.model.TXDeliveryFinished;
import com.mediasmiths.foxtel.wf.adapter.util.TxUtil;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;


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
	@Named("tx.delivery.location")
	private String txDeliveryLocation;
	@Inject
	@Named("ao.tx.delivery.location")
	private String aoTxDeliveryLocation;
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
	@Named("cerify.report.location.ardome")
	private String ardomeCerifyReportLocation;
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
		log.info("Ping request receivied");
		return "ping";
	}

	@Override
	@PUT
	@Path("/material/transferforqc")
	@Produces("application/xml")
	public AssetTransferForQCResponse transferMaterialForQC(AssetTransferForQCRequest req) throws MayamClientException
	{
		// doesnt actually do a transfer! just returns location (method needs renamed)

		log.info("Received AssetTransferForQCRequest " + req.toString());
		final String id = req.getAssetId();
		File destinationFile;

		if (req.isForTXDelivery())
		{
			boolean packageAO = mayamClient.isPackageAO(id);
			String tcoutputlocation = TxUtil.deliveryLocationForPackage(
					id,
					mayamClient,
					txDeliveryLocation,
					aoTxDeliveryLocation,
					packageAO);

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
				mayamClient.txDeliveryFailed(notification.getAssetId(), notification.getTaskID(), "AUTO QC FAILED");
				// attach qc report if qc is failed
				attachQcReports(notification.getAssetId(), notification.getJobName());
			}
			else
			{
				// id is an item id
				saveEvent("AutoQCFailed", notification, QC_EVENT_NAMESPACE);
				mayamClient.autoQcFailedForMaterial(notification.getAssetId(), notification.getTaskID());
				attachQcReports(notification.getAssetId(), notification.getJobName());

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

		// we dont actually need to transfer the media, just get its location
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
			// attach qc report if qc is passed
			attachQcReports(notification.getAssetId(), notification.getJobName());
			// update tasks status as required, next stage will be kicked off by intalio
		}
		else
		{
			mayamClient.autoQcPassedForMaterial(notification.getAssetId(), notification.getTaskID());
			attachQcReports(notification.getAssetId(), notification.getJobName());

		}
	}

	private void attachQcReports(final String assetID, final String jobName) throws MayamClientException
	{
		// find report pdfs
		IOFileFilter reportPDFAcceptor = new IOFileFilter()
		{

			@Override
			public boolean accept(File dir, String name)
			{
				return false; // dont look in sub directories
			}

			@Override
			public boolean accept(File file)
			{
				String name = FilenameUtils.getName(file.getAbsolutePath());
				if (name.startsWith(jobName))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		};

		Collection<File> reports = FileUtils.listFiles(new File(cerifyReportLocation), reportPDFAcceptor, reportPDFAcceptor);
		// attach report(s) to item

		for (File file : reports)
		{
			if (attachQcReports)
			{
				String ardomeFilePath = ardomeCerifyReportLocation + file.getName();

				log.info(String.format(
						"attach file {%s} to material using ardome path {%s}",
						file.getAbsolutePath(),
						ardomeFilePath));
				mayamClient.attachFileToMaterial(assetID, ardomeFilePath, cerifyReportArdomeserviceHandle);
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
				mayamClient.txDeliveryFailed(notification.getAssetId(), notification.getTaskID(), "AUTO QC ERROR");
			}
			else
			{
				// auto qc was for qc task
				saveEvent("CerifyQCError", notification, QC_EVENT_NAMESPACE);
				mayamClient.autoQcFailedForMaterial(notification.getAssetId(), notification.getTaskID());
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
		log.info(String.format(
				"Received notification of transcode Error asset ID %s isTX %b",
				notification.getAssetID(),
				notification.isForTXDelivery()));

		saveEvent("PersistentFailure", notification, TC_EVENT_NAMESPACE);

		try
		{
			if (notification.isForTXDelivery())
			{
				// auto qc was for tx delivery
				mayamClient.txDeliveryFailed(notification.getAssetID(), notification.getTaskID(), "TRANSCODE");
			}
			else
			{
				// auto qc was for export task
				mayamClient.exportFailed(notification.getTaskID());
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
	@Path("/tc/tcFailed")
	public void notifyTCFailed(TCFailureNotification notification) throws MayamClientException
	{

		log.info(String.format("Received notification of TC failure asset id %s", notification.getAssetID()));
		long taskId = notification.getTaskID();
		AttributeMap task = mayamClient.getTask(taskId);
		
		String taskListID = task.getAttribute(Attribute.TASK_LIST_ID);
		
		if (taskListID.equals(MayamButtonType.CAPTION_PROXY.getText()))
		{
			saveEvent("CaptionProxyFailed", notification, TC_EVENT_NAMESPACE);
		}
		else if (taskListID.equals(MayamButtonType.PUBLICITY_PROXY.getText()))
		{
			saveEvent("ClassificationProxyFailed", notification, TC_EVENT_NAMESPACE);
		}
		else if (taskListID.equals(MayamButtonType.COMPLIANCE_PROXY.getText()))
		{
			saveEvent("ComplianceProxyFailed", notification, TC_EVENT_NAMESPACE);
		}
		else
		{
			saveEvent("TCFailed", notification, TC_EVENT_NAMESPACE);
		}
	}

	@Override
	@PUT
	@Path("/tc/tcPassed")
	public void notifyTCPassed(TCPassedNotification notification) throws MayamClientException
	{
		log.info(String.format("Received notification of TC passed asset id %s", notification.getAssetID()));
		
		long taskId = notification.getTaskID();
		AttributeMap task = mayamClient.getTask(taskId);
		
		String taskListID = task.getAttribute(Attribute.TASK_LIST_ID);
		
		if (taskListID.equals(MayamButtonType.CAPTION_PROXY.getText()))
		{
			saveEvent("CaptionProxySuccess", notification, TC_EVENT_NAMESPACE);
		}
		else if (taskListID.equals(MayamButtonType.PUBLICITY_PROXY.getText()))
		{
			saveEvent("ClassificationProxySuccess", notification, TC_EVENT_NAMESPACE);
		}
		else if (taskListID.equals(MayamButtonType.COMPLIANCE_PROXY.getText()))
		{
			saveEvent("ComplianceProxySuccess", notification, TC_EVENT_NAMESPACE);
		}
		else
		{
			saveEvent("Transcoded", notification, TC_EVENT_NAMESPACE);
		}

		if (!notification.isForTXDelivery())
		{
			mayamClient.exportCompleted(notification.getTaskID());
		}
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
	public void notifyTXDeliveryFailed(TXDeliveryFailure notification) throws MayamClientException
	{
		log.fatal(String.format(
				"TX DELIVERY FAILURE FOR PACKAGE %s AT STAGE %s",
				notification.getPackageID(),
				notification.getStage()));

		mayamClient.txDeliveryFailed(notification.getPackageID(), notification.getTaskID(), notification.getStage());

		saveEvent("DeliveryFailed", notification, TX_EVENT_NAMESPACE);
	}

	@Override
	@GET
	@Path("/tx/deliveryLocation")
	@Produces("text/plain")
	public String deliveryLocationForPackage(@QueryParam("packageID") String packageID) throws MayamClientException
	{

		boolean packageAO = mayamClient.isPackageAO(packageID);
		return TxUtil.deliveryLocationForPackage(packageID, mayamClient, txDeliveryLocation, aoTxDeliveryLocation, packageAO);
	}

	@Override
	@GET
	@Path("/tx/delivery/writeSegmentXML")
	@Produces("text/plain")
	public boolean writeSegmentXML(@QueryParam("packageID") String packageID)
			throws MayamClientException,
			IOException,
			JAXBException
	{
		log.debug(String.format("Writing segment xml for package %s", packageID));

		String companion;
		
		boolean ao =mayamClient.isPackageAO(packageID);

		if (ao)
		{
			companion = getAOSegmentXML(packageID);
		}
		else
		{
			companion = getSegmentXML(packageID);
		}
		String deliveryLocation = deliveryLocationForPackage(packageID);

		File deliveryLocationFile = new File(deliveryLocation);
		deliveryLocationFile.mkdirs();

		File segmentXmlFile = new File(String.format("%s/%s.xml", deliveryLocation, packageID));
		File gxfFile =  new File(String.format("%s/%s.gxf", deliveryLocation, packageID));
		try
		{
			log.debug("Writing segmentinfo to " + segmentXmlFile);
			log.debug("Segment info is " + companion);
			FileUtils.writeStringToFile(segmentXmlFile, companion);
			
			if(ao){
				//should probably be its own intalio workflow step but doing it here for now
				return aoFXPtransfer(segmentXmlFile,gxfFile);
			}
			
			return true;
		}
		catch (IOException e)
		{
			log.error(String.format("Error writing companion xml for package %s", packageID), e);
			throw e;
		}

	}

	@Inject
	@Named("ao.tx.delivery.ftp.proxy.host")	
	private String aoFTPProxyHost;
	@Inject
	@Named("ao.tx.delivery.ftp.proxy.user")
	private String aoFTPProxyUser;
	@Inject
	@Named("ao.tx.delivery.ftp.proxy.pass")
	private String aoFTPProxyPass;
	
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.host")
	private String aoGXFFTPDestinationHost;
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.user")
	private String aoGXFFTPDestinationUser;
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.pass")
	private String aoGXFFTPDestinationPass;
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.path")
	private String aoGXFFTPDestinationPath;
	
	@Inject
	@Named("ao.tx.delivery.ftp.xml.host")
	private String aoXMLFTPDestinationHost;
	@Inject
	@Named("ao.tx.delivery.ftp.xml.user")
	private String aoXMLFTPDestinationUser;
	@Inject
	@Named("ao.tx.delivery.ftp.xml.pass")
	private String aoXMLFTPDestinationPass;
	@Inject
	@Named("ao.tx.delivery.ftp.xml.path")
	private String aoXMLFTPDestinationPath;
	
	private boolean aoFXPtransfer(File segmentXmlFile, File gxfFile)
	{

		String segmentFileName = FilenameUtils.getName(segmentXmlFile.getAbsolutePath());
		String gxfFileName = FilenameUtils.getName(gxfFile.getAbsolutePath());

		
		// first upload xml
		boolean xmlUpload =	ftpProxyTransfer(segmentFileName,String.format("%s%s", aoXMLFTPDestinationPath, segmentFileName), aoXMLFTPDestinationHost, aoXMLFTPDestinationUser, aoXMLFTPDestinationPass);

		if(xmlUpload){
			// next upload gxf
			boolean gxfUpload = ftpProxyTransfer(segmentFileName,String.format("%s%s", aoGXFFTPDestinationPath, gxfFileName), aoGXFFTPDestinationHost, aoGXFFTPDestinationUser, aoGXFFTPDestinationPass);
			
			if(gxfUpload){
				log.info("gxf upload complete");
				return true;
			}
			else{
				log.error("gxf upload failed");
				return false;
			}
		}
		else{
			log.error("xml segment upload failed");
			return false;
		}
	}

	private boolean ftpProxyTransfer(String sourceFileName,String targetPath, String targetHost, String targetUser, String targetPass){
		return Fxp.ftpProxyTransfer(sourceFileName, aoFTPProxyHost, aoFTPProxyUser, aoFTPProxyPass, targetPath, targetHost, targetUser, targetPass);
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

	@Override
	@PUT
	@Path("/tx/delivered")
	@Consumes("application/xml")
	public void notifiyTXDelivered(TXDeliveryFinished deliveryFinished) throws MayamClientException
	{
		mayamClient.txDeliveryCompleted(deliveryFinished.getPackageID(), deliveryFinished.getTaskID());

		TxDelivered txDelivered = new TxDelivered();
		txDelivered.setPackageID(deliveryFinished.getPackageID());
		txDelivered.setTaskID(deliveryFinished.getTaskID()+"");
		txDelivered.setTime((new Date()).toString());
		events.saveEvent("http://www.foxtel.com.au/ip/delivery", "Delivered", txDelivered);
	}

	@Override
	@GET
	@Path("/task/{taskid}/cancelled")
	@Produces("text/plain")
	public boolean isTaskCancelled(@PathParam("taskid") long taskid) throws MayamClientException
	{
		TaskState state = mayamClient.getTaskState(taskid);
		return state == TaskState.REJECTED || state == TaskState.REMOVED;
	}

	@Override
	@GET
	@Path("/mex/{materialid}/deliveryversion")
	@Produces("text/plain")
	public Integer deliveryVersionForMaterial(@PathParam("materialid") String materialID) throws MayamClientException
	{

		if (mayamClient.materialExists(materialID))
		{
			return mayamClient.getLastDeliveryVersionForMaterial(materialID);
		}

		throw new IllegalArgumentException("specified material does not exist");

	}

	@Override
	@GET
	@Path("/purge/list")
	@Produces("text/plain")
	public String getPurgePendingList() throws MayamClientException
	{
		List<AttributeMap> purgeTasks = mayamClient.getAllPurgeCandidatesPendingDeletion();

		StringBuilder sb = new StringBuilder();
		sb.append("TASK_ID\tASSET_ID\tASSET_TYPE\tASSET_TITLE\tDATE\n");

		for (AttributeMap attributeMap : purgeTasks)
		{
			sb.append(String.format(
					"%s\t%s\t%s\t%s\t%s\n",
					attributeMap.getAttributeAsString(Attribute.TASK_ID),
					attributeMap.getAttributeAsString(Attribute.ASSET_ID),
					attributeMap.getAttributeAsString(Attribute.ASSET_TYPE),
					attributeMap.getAttributeAsString(Attribute.ASSET_TITLE),
					attributeMap.getAttributeAsString(Attribute.OP_DATE)));
		}

		return sb.toString();

	}

	@Override
	@POST
	@Path("/purge/perform")
	@Produces("text/plain")
	public boolean performPendingPerges() throws MayamClientException
	{
		return mayamClient.deletePurgeCandidates();
	}




	////// -------------------- Eventing

	// temporary serialiser until moved within event service.

	private static JAXBSerialiser JAXB = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.events");




	protected void saveEvent(String name, String payload, String nameSpace)
	{
		events.saveEvent(name, payload, nameSpace);
	}


	// TC Events

	protected void saveEvent(String name, TCFailureNotification payload, String nameSpace)
	{
		try
		{
			TcFailureNotification tcFailedNotification = new TcFailureNotification();
			setTCNotification(payload, tcFailedNotification);
			events.saveEvent(name, JAXB.serialise(tcFailedNotification), nameSpace);
		}
		catch (Exception e)
		{
			log.error("Events unable to serialise message", e);
		}
	}

	protected void saveEvent(String name, TCTotalFailure payload, String nameSpace)
	{
		try
		{
			TcTotalFailure tcTotalFailure = new TcTotalFailure();
			setTCNotification(payload, tcTotalFailure);
			events.saveEvent(name, JAXB.serialise(tcTotalFailure), nameSpace);
		}
		catch (Exception e)
		{
			log.error("Events unable to serialise message", e);
		}
	}

	private void setTCNotification(final TCNotification payload, final TcNotification tcNotification)
	{
		tcNotification.setAssetID(payload.getAssetID());
		tcNotification.setTaskID(payload.getTitle());
		tcNotification.setTaskID(payload.getTaskID() + "");
	}



	protected void saveEvent(String name, TCPassedNotification payload, String nameSpace)
	{
		try
		{
			TcPassedNotification tcPassedNotification = new TcPassedNotification();
			setTCNotification(payload, tcPassedNotification);
			events.saveEvent(name, JAXB.serialise(tcPassedNotification), nameSpace);
		}
		catch (Exception e)
		{
			log.error("Events unable to serialise message", e);
		}
	}

	// Auto QC Event

	protected void saveEvent(String name, AutoQCErrorNotification payload, String nameSpace)
	{
		try
		{
			com.mediasmiths.foxtel.ip.common.events.AutoQCErrorNotification qcErrorNotification = new com.mediasmiths.foxtel.ip.common.events.AutoQCErrorNotification();
			setQCNotification(payload, qcErrorNotification);
			events.saveEvent(name, JAXB.serialise(qcErrorNotification), nameSpace);
		}
		catch (Exception e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}


	protected void saveEvent(String name, AutoQCFailureNotification payload, String nameSpace)
	{
		try
		{
			com.mediasmiths.foxtel.ip.common.events.AutoQCFailureNotification autoQCFailureNotification = new com.mediasmiths.foxtel.ip.common.events.AutoQCFailureNotification();
			setQCNotification(payload, autoQCFailureNotification);
			events.saveEvent(name, JAXB.serialise(autoQCFailureNotification), nameSpace);
		}
		catch (Exception e)
		{
			log.error("Events unable to serialise message", e);
		}
	}

	protected void saveEvent(String name, AutoQCPassNotification payload, String nameSpace)
	{
		try
		{
			com.mediasmiths.foxtel.ip.common.events.AutoQCPassNotification qcFailureNotification = new com.mediasmiths.foxtel.ip.common.events.AutoQCPassNotification();
			setQCNotification(payload, qcFailureNotification);
			events.saveEvent(name, JAXB.serialise(qcFailureNotification), nameSpace);
		}
		catch (Exception e)
		{
			log.error("Events unable to serialise message", e);
		}
	}

	private void setQCNotification(final com.mediasmiths.foxtel.wf.adapter.model.AutoQCResultNotification payload,
	                               final com.mediasmiths.foxtel.ip.common.events.AutoQCResultNotification qcErrorNotification)
	{
		qcErrorNotification.setAssetId(payload.getAssetId());
		qcErrorNotification.setForTXDelivery(payload.isForTXDelivery());
		qcErrorNotification.setJobId(payload.getJobName());
	}

	// TX Delivery

	protected void saveEvent(String name, TXDeliveryFailure payload, String nameSpace)
	{
		try
		{
			TxDeliveryFailure deliveryFailure = new TxDeliveryFailure();
			deliveryFailure.setPackageID(payload.getPackageID());
			deliveryFailure.setStage(payload.getStage());
			deliveryFailure.setJobName(payload.getTaskID() + "");
			events.saveEvent(name, JAXB.serialise(deliveryFailure), nameSpace);
		}
		catch (Exception e)
		{
			log.error("Events unable to serialise message", e);
		}
	}



}
