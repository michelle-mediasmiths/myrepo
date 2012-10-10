package com.mediasmiths.foxtel.mayam.adapter.service;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


import com.mediasmiths.foxtel.mayam.adapter.model.MaterialTransferForQCRequest;

public class MayamAdapterRestServiceImpl implements MayamAdapterRestService
{

	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping()
	{
		return "ping";
	}

	@PUT
	@Path("/material/transferforqc")
	@Produces("text/plain")
	public Boolean transferMaterialForQC(MaterialTransferForQCRequest req)
	{
		final String materialID = req.getMaterialID();
		final URI destinaion = req.getDestination();
		
		return Boolean.TRUE;
	}

}
