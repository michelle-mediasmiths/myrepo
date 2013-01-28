package com.mediasmiths.stdEvents.ui.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

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
	
	@GET
	@Path("/bms.html")
	@Produces("text/html")
	public String getBms();
	
	@GET
	@Path("/content_pickup.html")
	@Produces("text/html")
	public String getContentPickup();

	@GET
	@Path("/transcode.html")
	@Produces("text/html")
	public String getTranscode();
	
	@GET
	@Path("/qc.html")
	@Produces("text/html")
	public String getQC();
	
	@GET
	@Path("/delivery.html")
	@Produces("text/html")
	public String getDelivery();
	
	@GET
	@Path("/system.html")
	@Produces("text/html")
	public String getSystem();
}
