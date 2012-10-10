package com.mediasmiths.foxtel.mayam.adapter.service;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


import com.mediasmiths.foxtel.mayam.adapter.model.MaterialTransferForQCRequest;
import com.mediasmiths.foxtel.mayam.adapter.model.MaterialTransferForQCResponse;
import com.mediasmiths.mayam.MayamClient;

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
	public MaterialTransferForQCResponse transferMaterialForQC(MaterialTransferForQCRequest req)
	{
		final String materialID = req.getMaterialID();

		
		
		return new MaterialTransferForQCResponse();
	}

}
