package com.mediasmiths.foxtel.wf.adapter.service;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCErrorNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCPassNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.GetQCProfileResponse;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.TCFailureNotification;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;

public class WFAdapterRestServiceImpl implements WFAdapterRestService
{

	private final static Logger log = Logger.getLogger(WFAdapterRestServiceImpl.class);

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
			mayamClient.transferMaterialToLocation(id, destination);
		}
		else
		{
			destination = materialQCLocation.resolve(filename);
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
		final String packageID = req.getPackageID();
		final String filename = packageID + ".mxf";
		final URI destination = materialTCLocation.resolve(filename);

		mayamClient.transferMaterialToLocation(packageID, destination);
		return new MaterialTransferForTCResponse(filename);
	}

	@Override
	@PUT
	@Path("/qc/tcFailed")
	public void notifyTCFailed(TCFailureNotification notification) throws MayamClientException
	{
		log.info(String.format(
				"Received notification of TC failure Paciage ID %s isTX %b",
				notification.getPackageID(),
				notification.isTXDelivery()));

		try
		{
			if (notification.isTXDelivery())
			{
				// transcode was for tx delivery
				mayamClient.failTaskForAsset(MayamTaskListType.TX_DELIVERY, notification.getPackageID());
			}
			else
			{
				// transcode was for extended publishing
				// TODO do something
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
	@Path("/qc/autoQcPassed")
	public void notifyAutoQCPassed(AutoQCPassNotification notification)
	{
		log.info(String.format(
				"Received notification of Auto QC Pass ID %s isTX %b",
				notification.getAssetId(),
				notification.isForTXDelivery()));
		// TODO: handle
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

		//TODO: add entry to general error task list as investigation is required
		
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
				mayamClient.failTaskForAsset(MayamTaskListType.QC_VIEW, notification.getAssetId());
			}
		}
		catch (MayamClientException e)
		{
			log.error("Failed to fail task!", e);
			throw e;
		}
	}

}
