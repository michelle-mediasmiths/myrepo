package com.mediasmiths.foxtel.mayam.adapter.service;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.mediasmiths.foxtel.mayam.adapter.model.MaterialTransferForQCRequest;
import com.mediasmiths.foxtel.mayam.adapter.model.MaterialTransferForQCResponse;
import com.mediasmiths.mayam.MayamClientException;

@Path("/mayam")
public interface MayamAdapterRestService
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
	@Path("/material/transferforqc")
	@Produces("text/plain")
	public MaterialTransferForQCResponse transferMaterialForQC(MaterialTransferForQCRequest req) throws MayamClientException;
	 
}
