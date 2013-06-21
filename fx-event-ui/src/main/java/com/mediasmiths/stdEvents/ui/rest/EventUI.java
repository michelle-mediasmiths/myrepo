package com.mediasmiths.stdEvents.ui.rest;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Path("/ui")
public interface EventUI
{	
	@POST
	@Path("/save_report")
	@Consumes("application/xml")
	public void saveReport(EventEntity event);
	
	@GET
	@Path("/mule_demo/{path}")
	@Consumes("text/plain")
	public void muleDemo(@PathParam("path")String path);
	
	@GET
	@Path("/")
	@Produces("text/html")
	public String getIndex();

	@GET
	@Path("/events.html")
	@Produces("text/html")
	public String getEvents();
	
	@GET
	@Path("/event/id")
	@Produces("text/html")
	public String getEvent(@QueryParam("id")Long id);
	
	@DELETE
	@Path("/delete/id")
	@Produces("text/html")
	public String deleteEvent(@QueryParam("id")Long id);
	
	@GET
	@Path("/atom_rss_feed.html")
	@Produces("text/html")
	public String getFeed();
}
