package com.mediasmiths.foxtel.wf.adapter.service;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
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
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
//import com.mediasmiths.stdEvents.persistence.db.entity.EventEntity;
//import com.mediasmiths.stdEvents.persistence.rest.api.EventAPI;

public class WFAdapterRestServiceImpl implements WFAdapterRestService
{

	private final static Logger log = Logger.getLogger(WFAdapterRestServiceImpl.class);

	// @Inject
	// private EventAPI events;
	// @Inject
	// private Marshaller marshaller;
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
	@Named("stub.out.mayam")
	private boolean stubMayam;

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
		final String filename = req.getAssetId() + ".mxf";
		final URI destination;
		if (req.isForTXDelivery())
		{
			// TODO work out what to do here, a transfer might not be required if part of tx delivery workflow
			destination = materialQCLocation.resolve(filename);

			if (!stubMayam)
				mayamClient.transferMaterialToLocation(id, destination);
		}
		else
		{
			destination = materialQCLocation.resolve(filename);
			if (!stubMayam)
				mayamClient.transferMaterialToLocation(id, destination);
		}

		return new AssetTransferForQCResponse(filename);
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
				if (!stubMayam)
					mayamClient.failTaskForAsset(MayamTaskListType.TX_DELIVERY, notification.getAssetId());
			}
			else
			{
				// id is an item id
				if (!stubMayam)
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
			if (!stubMayam)
			{
				materialID = mayamClient.getMaterialIDofPackageID(assetID);
			}
			else
			{
				materialID = assetID;
			}
		}
		else
		{
			materialID = assetID;
		}

		if (!stubMayam)
			mayamClient.transferMaterialToLocation(assetID, destination);

		return new MaterialTransferForTCResponse(filename);
	}

	@Override
	@PUT
	@Path("/qc/autoQcPassed")
	public void notifyAutoQCPassed(AutoQCPassNotification notification)
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
			// update tasks status as required, next stage will be kicked off by intalio
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

		// TODO: add entry to general error task list as investigation is required
		saveEvent("AutoQCError", notification, "http://www.foxtel.com.au/ip/qc");
		try
		{
			if (notification.isForTXDelivery())
			{
				// auto qc was for tx delivery
				if (!stubMayam)
					mayamClient.failTaskForAsset(MayamTaskListType.TX_DELIVERY, notification.getAssetId());
			}
			else
			{
				// auto qc was for qc task
				if (!stubMayam)
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
	@Path("/tc/tcFailedTotal")
	public void notifyTCFailedTotal(TCTotalFailure notification) throws MayamClientException
	{
		// TODO Auto-generated method stub
		log.info(String.format("Received notification of TC totalfailure Paciage ID %s ", notification.getPackageID()));

		saveEvent("persistentfailure", notification, "http://www.foxtel.com.au/ip/tc");

	}

	@Override
	@PUT
	@Path("/tc/tcFailed")
	public void notifyTCFailed(TCFailureNotification notification) throws MayamClientException
	{

		// TODO Auto-generated method stub
		log.info(String.format("Received notification of TC failure Paciage ID %s", notification.getPackageID()));
		saveEvent("failed", notification, "http://www.foxtel.com.au/ip/tc");

	}

	@Override
	@PUT
	@Path("/tc/tcPassed")
	public void notifyTCPassed(TCPassedNotification notification) throws MayamClientException
	{
		// TODO Auto-generated method stub
		log.info(String.format("Received notification of TC passed Paciage ID %s", notification.getPackageID()));

		saveEvent("Transcoded", notification, "http://www.foxtel.com.au/ip/tc");

	}

	@Override
	@GET
	@Path("/tx/companionXMLforTXPackage")
	@Produces("application/xml")
	public ProgrammeMaterialType getCompanionXMLForTXPackage(@QueryParam("packageID") String packageID)
			throws MayamClientException
	{
		String materialID = mayamClient.getMaterialIDofPackageID(packageID);
		ProgrammeMaterialType materialType = mayamClient.getProgrammeMaterialType(materialID);

		Package p;

		if (!stubMayam)
		{
			p = mayamClient.getPresentationPackage(packageID);
		}
		else
		{
			p = new Package();
		}

		List<Package> packages = materialType.getPresentation().getPackage();
		packages.add(p);

		return materialType;
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
		//TODO implement
		return true;
	}

	@Override
	@PUT
	@Path("/tx/failed")
	@Consumes("application/xml")
	public void notifyTXDeliveryFailed(TXDeliveryFailure notification)
	{
		log.fatal(String.format("TX DELIVERY FAILURE FOR PACKAGE %s AT STAGE %s"));
		//TODO fire event email people etc
	}

	@Override
	@GET
	@Path("/tx/transcodeOutputLocation")
	@Produces("text/plain")
	public String transcodeOutputLocationForPackage(@QueryParam("packageID") String packageID)
	{
		// TODO implement
		return tcoutputlocation;
	}

}