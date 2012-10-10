package com.mediasmiths.foxtel.mayam.adapter.service;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.mediasmiths.foxtel.mayam.adapter.model.MaterialTransferForQCRequest;

@Path("/mayam")
public interface MayamAdapterRestService
{


	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();
	
	@PUT
	@Path("/material/transferforqc")
	@Produces("text/plain")
	public Boolean transferMaterialForQC(MaterialTransferForQCRequest req);
	
}
