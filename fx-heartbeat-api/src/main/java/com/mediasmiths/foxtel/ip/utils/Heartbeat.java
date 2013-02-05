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
@Path("/heartbeat")
public interface Heartbeat
{

	/**
	 * a heart beat has occurred
	 */
	@PUT
	@Path("/beat")
	public void beat();


	/**
	 * set the status of client to the argument state
	 * @param status the boolean state of the client active == true / passive == false
	 * @return
	 */
	@GET
	@Path("/setStatus")
	public void setStatus(@QueryParam("status")boolean status);


	/**
	 *
	 * @return the current status of client active == true | passive == false
	 */
	@GET
	@Path("/status")
	@Produces("text/plain")
	public boolean getStatus();



}
