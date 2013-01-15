package com.mediasmiths.foxtel.wf.adapter.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import javax.swing.border.TitledBorder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIF;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCErrorNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCPassNotification;
import com.mediasmiths.foxtel.wf.adapter.model.GetQCProfileResponse;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.TCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCPassedNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCTotalFailure;
import com.mediasmiths.foxtel.wf.adapter.model.TXDeliveryFailure;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
//import com.mediasmiths.stdEvents.persistence.db.entity.EventEntity;
//import com.mediasmiths.stdEvents.persistence.rest.api.EventAPI;

public class WFAdapterRestServiceImpl implements WFAdapterRestService
{

	private final static Logger log = Logger.getLogger(WFAdapterRestServiceImpl.class);

	// @Inject
	// private EventAPI events;

	@Inject
	private MayamClient mayamClient;
	@Inject
	@Named("qc.material.location")
	private URI materialQCLocation;
	@Inject
	@Named("tc.material.location")
	private URI materialTCLocation;
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
		log.info("Received AssetTransferForQCRequest " + req.toString());
		final String id = req.getAssetId();
		// TODO change this to mxf once we have proper profiles
		final String filename = req.getAssetId() + ".mov";
		File destinationFile;

		if (req.isForTXDelivery())
		{
			// for tx delivery we return the location of transcoded package
			
			// TODO switch this to gxf once we are creating such!
			String ret = tcoutputlocation + id + "/" + id + ".mov";
			destinationFile = new File(ret);
		}
		else
		{
//			destination = materialQCLocation.resolve(filename);
			String destination = mayamClient.pathToMaterial(id);

			destinationFile = new File(destination);
		}

		return new AssetTransferForQCResponse(destinationFile.getAbsolutePath());
	}

	private void transferAsset(final String id, final URI destination) throws MayamClientException
	{
		try
		{
			log.info(String.format("Transferring material %s to location %s", id, destination.toString()));
			FileUtils.copyFile(new File("/storage/qcmedialocation/test.mxf"), new File(destination));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new MayamClientException(MayamClientErrorCode.FAILURE, e);
		}
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
		saveEvent("AutoQCFailed", notification, "http://www.foxtel.com.au/ip/qc");

		try
		{
			if (notification.isForTXDelivery())
			{
				// id is a package id

				mayamClient.failTaskForAsset(MayamTaskListType.TX_DELIVERY, notification.getAssetId());
			}
			else
			{
				// id is an item id

				mayamClient.failTaskForAsset(MayamTaskListType.QC_VIEW, notification.getAssetId());

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
		final String filename = assetID + ".mxf";
		final URI destination = materialTCLocation.resolve(filename);

		final String materialID;

		if (req.isForTX())
		{

			materialID = mayamClient.getMaterialIDofPackageID(assetID);

		}
		else
		{
			materialID = assetID;
		}

		
		//TODO caluclate destination and perform transfer
		String path = mayamClient.pathToMaterial(assetID);
		
		File destinationFile = new File(path);
		return new MaterialTransferForTCResponse(destinationFile.getAbsolutePath());
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
		// TODO: handle
		saveEvent("AutoQCPassed", notification, "http://www.foxtel.com.au/ip/qc");

		if (notification.isForTXDelivery())
		{
			// update tasks status as required, next stage will be kicked off by intalio
		}
		else
		{
			mayamClient.autoQcPassedForMaterial(notification.getAssetId());
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

		// TODO: add entry to general error task list as investigation is required (ditto for equivalent TC failure methods)
		saveEvent("AutoQCError", notification, "http://www.foxtel.com.au/ip/qc");
		try
		{
			if (notification.isForTXDelivery())
			{
				// auto qc was for tx delivery

				mayamClient.failTaskForAsset(MayamTaskListType.TX_DELIVERY, notification.getAssetId());
			}
			else
			{
				// auto qc was for qc task
				mayamClient.autoQcFailedForMaterial(notification.getAssetId());
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

		saveEvent("persistentfailure", notification, "http://www.foxtel.com.au/ip/tc");

	}

	@Override
	@PUT
	@Path("/tc/tcFailed")
	public void notifyTCFailed(TCFailureNotification notification) throws MayamClientException
	{

		log.info(String.format("Received notification of TC failure Package ID %s", notification.getPackageID()));
		saveEvent("failed", notification, "http://www.foxtel.com.au/ip/tc");

	}

	@Override
	@PUT
	@Path("/tc/tcPassed")
	public void notifyTCPassed(TCPassedNotification notification) throws MayamClientException
	{
		log.info(String.format("Received notification of TC passed Package ID %s", notification.getPackageID()));

		saveEvent("Transcoded", notification, "http://www.foxtel.com.au/ip/tc");

	}

	// TODO reenable events stuff after events api has been extracted
	protected void saveEvent(String name, String payload, String nameSpace)
	{
		// try
		// {
		// EventEntity event = new EventEntity();
		// event.setEventName(name);
		// event.setNamespace(nameSpace);
		//
		// event.setPayload(payload);
		// event.setTime(System.currentTimeMillis());
		// events.saveReport(event);
		// }
		// catch (RuntimeException re)
		// {
		// log.error("error saving event" + name, re);
		// }

	}

	protected void saveEvent(String name, Object payload, String nameSpace)
	{
		// try
		// {
		// ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// marshaller.marshal(payload, baos);
		// String sPayload = baos.toString("UTF-8");
		// saveEvent(name, sPayload, nameSpace);
		// }
		// catch (JAXBException e)
		// {
		// log.error("error saving event" + name, e);
		// }
		// catch (UnsupportedEncodingException e)
		// {
		// log.error("error saving event" + name, e);
		// }

	}

	@Override
	@GET
	@Path("/tx/autoQCRequired")
	@Produces("text/plain")
	public Boolean autoQCRequiredForPackage(@QueryParam("packageID") String packageID)
	{
		// TODO implement
//		return true;
		return false; //returning false due to lack of cerify at forge
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
		// TODO fire event notification
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