package com.mediasmiths.foxtel.wf.adapter.service;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForQCResponse;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class WFAdapterRestServiceImpl implements WFAdapterRestService
{
	
	@Inject private MayamClient mayamClient;
	@Named("qc.material.location") private URI materialQCLocation;

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
	public MaterialTransferForQCResponse transferMaterialForQC(MaterialTransferForQCRequest req) throws MayamClientException
	{
		final String materialID = req.getMaterialID();
		final URI destination = materialQCLocation.resolve(req.getMaterialID()+".mxf");
		
		mayamClient.transferMaterialToLocation(materialID, destination);
		return new MaterialTransferForQCResponse(destination);
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

}
