package com.mediasmiths.foxtel.wf.adapter.service;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForQCResponse;
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
	public MaterialTransferForQCResponse transferMaterialForQC(MaterialTransferForQCRequest req) throws MayamClientException
	{
		log.info("Received MaterialTransferForQCRequest " + req.toString());
		final String materialID = req.getMaterialID();
		final String filename = req.getMaterialID() + ".mxf";
		final URI destination = materialQCLocation.resolve(filename);

		mayamClient.transferMaterialToLocation(materialID, destination);
		return new MaterialTransferForQCResponse(filename);
	}

	@Override
	@GET
	@Path("/qc/profile")
	@Produces("text/plain")
	public String getProfileForQc(@QueryParam("materialID") String materialID, @QueryParam("isForTX") boolean isForTXDelivery)
	{
		// TODO implement
		return "FoxtelK2";
	}

	@Override
	@PUT
	@Path("/qc/autoQcFailed")
	public void notifyAutoQCFailed(AutoQCFailureNotification notification) throws MayamClientException
	{
		log.info(String.format("Received notification of Auto QC failure ID %s isTX %b", notification.getAssetId(), notification.isForTXDelivery()));
		
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
			log.error("Failed to fail task!",e);
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
		log.info(String.format("Received notification of TC failure Paciage ID %s isTX %b", notification.getPackageID(), notification.isTXDelivery()));
		
		try
		{
			if (notification.isTXDelivery())
			{
				//transcode was for tx delivery
				mayamClient.failTaskForAsset(MayamTaskListType.TX_DELIVERY, notification.getPackageID());
			}
			else
			{
				//transcode was for extended publishing
				//TODO do something
			}
		}
		catch (MayamClientException e)
		{
			log.error("Failed to fail task!",e);
			throw e;
		}
		
	}

}
