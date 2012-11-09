package com.mediasmiths.foxtel.fs.service;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.mediasmiths.foxtel.fs.model.DeleteRequest;
import com.mediasmiths.foxtel.fs.model.DeleteResponse;
import com.mediasmiths.foxtel.fs.model.MoveRequest;
import com.mediasmiths.foxtel.fs.model.MoveResponse;

@Path("/fs")
public interface FSRestService
{
	
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();
	
	@PUT
	@Path("/delete")
	@Produces("application/xml")
	public DeleteResponse deleteFile(DeleteRequest dr) throws FSAdapterException; 
	
	@PUT
	@Path("/move")
	@Produces("application/xml")
	public MoveResponse movefile(MoveRequest mr) throws FSAdapterException;
	
	
}
