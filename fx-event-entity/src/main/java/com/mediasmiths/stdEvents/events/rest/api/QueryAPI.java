package com.mediasmiths.stdEvents.events.rest.api;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

/**
 * Used to query the database with criteria to create specific lists of events
 * Implemented by QueryAPIImpl
 * @author alisonboal
 *
 */
@Path("query_api")
public interface QueryAPI
{
	@GET
	@Path("/get_report/{namespace}/{eventname}")
	@Produces("text/plain")
	public List<EventEntity> getEvents (@PathParam("namespace")String namespace, @PathParam("eventname")String eventName);

	@GET
	@Path("/allEvents")
	public List<EventEntity> getAllEvents();
	
	@GET
	@Path("getbynamespace/{namespace}")
	@Produces("text/plain")
	public List<EventEntity> getByNamespace(@PathParam("namespace")String namespace);
	
	@GET
	@Path("getbyeventname/{eventname}")
	@Produces("text/plain")
	public List<EventEntity> getByEventName(@PathParam("eventname")String eventName);
	
	@GET
	@Path("/event/{id}")
	@Produces("text/plain")
	public EventEntity getById (@PathParam("id")Long id);
	
	@DELETE
	@Path("/event/{id}")
	@Produces("text/plain")
	public void deleteById (@PathParam("id")Long id);

	@GET
	@Path("/avCompletionTime")
	@Produces("text/plain")
	public String getAvCompletionTime(List<EventEntity> events);
	
	@GET
	@Path("getbynamespacewindow/{namespace}/{max}")
	@Produces("text/plain")
	public List<EventEntity> getByNamespaceWindow(@PathParam("namespace")String namespace, @PathParam("max")int max);
	
	@GET
	@Path("/get_report-window/{namespace}/{eventname}/{max}")
	@Produces("text/plain")
	public List<EventEntity> getEventsWindow (@PathParam("namespace")String namespace, @PathParam("eventname")String eventName, @PathParam("max")int max);
	
	@GET
	@Path("/getbyeventnamewindow/{eventname}/{max}")
	@Produces("text/plain")
	public List<EventEntity> getByEventNameWindow (@PathParam("eventname")String eventName, @PathParam("max")int max);
	
	
}
