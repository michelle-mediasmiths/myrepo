package com.mediasmiths.foxtel.tc.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Path("/transcoder")
public interface TCRestService
{

	/**
	 * Simple ping method to check service is up
	 *
	 * @return
	 */
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();

	@POST
	@Path("/jobs")
	@Produces("text/plain")
	@Consumes("application/xml")
	public String createJob(TCJobParameters parameters) throws Exception;

	@GET
	@Path("/job/{guid}")
	@Produces("application/xml")
	public TCJobInfo queryJob(@PathParam("guid") String guid) throws Exception;

	@PUT
	@Path("/job/{guid}/priority")
	public void setJobPriority(@PathParam("guid") String guid, @QueryParam("priority") Integer newPriority) throws Exception;
}
