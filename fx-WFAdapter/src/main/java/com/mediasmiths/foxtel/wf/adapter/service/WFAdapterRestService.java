package com.mediasmiths.foxtel.wf.adapter.service;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForQCResponse;
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
	public MaterialTransferForQCResponse transferMaterialForQC(MaterialTransferForQCRequest req) throws MayamClientException;
	
	
	@PUT
	@Path("/tc/transferfortc")
	@Produces("application/xml")
	public String transferMaterialForTC(String materialID) throws MayamClientException;
	
	
	/**
	 * called to lookup the name of the qc profile that should be used for a given material 
	 * 
	 * TODO: (if its for txDelivery does that mean we should expect a packageid or just refer to a file or what?)
	 * 
	 * @param materialID
	 * @param isForTXDelivery
	 * @return
	 */
	@GET
	@Path("/qc/profile")
	@Produces("text/plain")
	public String getProfileForQc(@QueryParam("materialID") String materialID, @QueryParam("isForTX") boolean isForTXDelivery);
	
}
