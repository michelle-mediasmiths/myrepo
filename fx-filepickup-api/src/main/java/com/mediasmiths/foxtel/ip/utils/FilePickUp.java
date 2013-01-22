package com.mediasmiths.foxtel.ip.utils;


import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 *
 * Generic interface for a file pick up agent.  That will receive new file notifications
 *
 * Author: Harmer
 */
@Path("/file")
public interface FilePickUp
{
	/**
	 * @param filePath
	 * 		the fully qualified path to a new file that has been dropped in a pick up location.
	 */
	@PUT
	@Path("/new")
	public void newFile(@QueryParam("name") String filePath);

	/**
	 * @return a formatted list of files that are currently awaiting processing.
	 */
	@GET
	@Path("/pending")
	@Produces("text/plain")
	public String getPending();

	/**
	 * @return a formatted summary of files processed, and files currently pending.
	 */
	@GET
	@Path("/status")
	@Produces("text/plain")
	public String getStatus();


	/**
	 * clear all files that are currently set for processing.
	 */
	@PUT
	@Path("/clear")
	public void clear();


}
