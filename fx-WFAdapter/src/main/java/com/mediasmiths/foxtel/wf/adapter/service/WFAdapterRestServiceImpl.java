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
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForQCResponse;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class WFAdapterRestServiceImpl implements WFAdapterRestService
{
	
	private final static Logger log = Logger.getLogger(WFAdapterRestServiceImpl.class);
	
	@Inject private MayamClient mayamClient;
	@Inject @Named("qc.material.location") private URI materialQCLocation;

	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping()
	{
		return "ping";
	} 

	@PUT
	@Path("/material/transferforqc")
	@Produces("application/xml")
	public MaterialTransferForQCResponse transferMaterialForQC(MaterialTransferForQCRequest req) throws MayamClientException
	{
		log.info("Received MaterialTransferForQCRequest "+req.toString());
		final String materialID = req.getMaterialID();
		final String filename = req.getMaterialID()+".mxf";
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

}
