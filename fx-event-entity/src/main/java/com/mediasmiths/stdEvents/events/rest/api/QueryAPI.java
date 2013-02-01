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
	@Path("/getall/{type}")
	@Produces ("text/plain")
	public List<?> getAll(@PathParam("type")String type);
	
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
	@Path("/byMedia/{namespace}/{eventname}/{media}")
	@Produces("text/plain")
	public List<EventEntity> getByMedia(@PathParam("namespace")String namespace, @PathParam("eventname")String eventName,@PathParam("media") String media);
	
	@GET
	@Path("/delivered")
	@Produces("text/plain")
	public List<EventEntity> getDelivered();
	
	@GET
	@Path("/notDelivered")
	@Produces("text/plain")
	public List<EventEntity> getOutstanding();
	
	@GET
	@Path("/overdue")
	@Produces("text/plain")
	public List<EventEntity> getOverdue();
	
	@GET
	@Path("/expiring")
	@Produces("text/plain")
	public List<EventEntity> getExpiring();
	
	@GET
	@Path("/protected")
	@Produces("text/plain")
	public List<EventEntity> getProtected();
	
	@GET
	@Path("/compliance")
	@Produces("text/plain")
	public List<EventEntity> getCompliance();
	
	@GET
	@Path("/totalQAd")
	@Produces("text/plain")
	public List<EventEntity> getTotalQAd();
	
	@GET
	@Path("/failedQA")
	@Produces("text/plain")
	public List<EventEntity> getTotalFailedQA();
	
	@GET
	@Path("/totalQcd")
	@Produces("text/plain")
	public List<EventEntity> getTotalQCd();
	
	@GET
	@Path("/failedQc")
	@Produces("text/plain")
	public List<EventEntity> getFailedQc();
	
	@GET
	@Path("/operatorOverridden")
	@Produces("text/plain")
	public List<EventEntity> getOperatorOverridden();
	
	@GET
	@Path("/getExpiringPurged")
	@Produces("text/plain")
	public List<EventEntity> getExpiringPurged();
	
	@GET
	@Path("/purgeProtected")
	@Produces("text/plain")
	public List<EventEntity> getPurgeProtected();
	
	@GET
	@Path("/purgePosponed")
	@Produces("text/plain")
	public List<EventEntity> getPurgePosponed();
	
	@GET
	@Path("/totalPurged")
	@Produces("text/plain")
	public List<EventEntity> getTotalPurged();
	
	@GET
	@Path("/total")
	@Produces("text/plain")
	public int getTotal(List<EventEntity> events);
	
	@GET
	@Path("/length")
	@Produces("text/plain")
	public int getLength(List<EventEntity> events);
	
	@GET
	@Path("/format")
	@Produces("text/plain")
	public List<String> getFormat(List<EventEntity> events);
}
