package com.mediasmiths.foxtel.wf.adapter.service;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.mediasmiths.foxtel.wf.adapter.model.AutoQCErrorNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCPassNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.GetQCProfileResponse;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.TCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCPassedNotification;
import com.mediasmiths.mayam.MayamClientException;

@Path("/wf")
public interface WFAdapterRestService
{


	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();
	
	/**
	 * called to transfer material to the configured location for qc of material arriving from partners
	 * 
	 * @param req
	 * @return
	 * @throws MayamClientException 
	 */
	@PUT
	@Path("/qc/transferforqc")
	@Produces("application/xml")
	public AssetTransferForQCResponse transferMaterialForQC(AssetTransferForQCRequest req) throws MayamClientException;
	
	
	/**
	 * called to lookup the name of the qc profile that should be used for a given material 
	 * 
	 * TODO: (if its for txDelivery does that mean we should expect a packageid or just refer to a file or what?)
	 * 
	 * @param materialID
	 * @param isForTXDelivery
	 * @return
	 * @throws MayamClientException 
	 */
	@GET
	@Path("/qc/profile")
	@Produces("application/xml")
	public GetQCProfileResponse getProfileForQc(@QueryParam("assetID") String assetID, @QueryParam("isForTX") boolean isForTXDelivery) throws MayamClientException;


	@PUT
	@Path("/qc/autoQcFailed")
	public void notifyAutoQCFailed(AutoQCFailureNotification notification) throws MayamClientException;
	
	@PUT
	@Path("/qc/autoQcPassed")
	public void notifyAutoQCPassed(AutoQCPassNotification notification);
	
	@PUT
	@Path("/qc/autoQcError")
	public void notifyAutoQCError(AutoQCErrorNotification notification) throws MayamClientException;
	
	/**
	 * indates an error transcoding
	 * @param notification
	 * @return 
	 * @throws MayamClientException
	 */
	@PUT
	@Path("/tc/tcFailed")
	@Produces("application/xml")
	public String notifyTCFailed(TCFailureNotification notification) throws MayamClientException;
	
	/**
	 * indicates there have been multiple errors transcoding and the workflow has given up
	 * @param notification
	 * @throws MayamClientException
	 */
	@PUT
	@Path("/tc/tcFailedTotal")
	@Produces("application/xml")
	public void notifyTCFailedTotal(TCFailureNotification notification) throws MayamClientException;
	
	
	/**
	 * indates an error transcoding
	 * @param notification
	 * @throws MayamClientException
	 */
	@PUT
	@Path("/tc/tcPassed")
	@Produces("application/xml")
	public void notifyTCPassed(TCPassedNotification notification) throws MayamClientException;
	
	@PUT
	@Path("/tc/transferfortc")
	@Produces("application/xml")
	public MaterialTransferForTCResponse transferMaterialForTC(MaterialTransferForTCRequest materialID) throws MayamClientException;

	
}

