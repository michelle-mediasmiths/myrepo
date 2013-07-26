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
	/***
	 * Simple ping method to test service is up
	 * @return
	 */
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();
	
	/**
	 * Deletes a specified file
	 * @param dr
	 * @return
	 * @throws FSAdapterException
	 */
	@PUT
	@Path("/delete")
	@Produces("application/xml")
	public DeleteResponse deleteFile(DeleteRequest dr) throws FSAdapterException; 
	
	/**
	 * Moves a file or folder to another location
	 * @param mr
	 * @return
	 * @throws FSAdapterException
	 */
	@PUT
	@Path("/move")
	@Produces("application/xml")
	public MoveResponse movefile(MoveRequest mr) throws FSAdapterException;
	
	@PUT
	@Path("/selectMostRecent")
	@Produces("application/xml")
	public boolean selectMostRecent(String filepath) throws FSAdapterException;
	
	@PUT
	@Path("/cleanup")
	@Produces("application/xml")
	public boolean cleanup(String filepath) throws FSAdapterException;
}
