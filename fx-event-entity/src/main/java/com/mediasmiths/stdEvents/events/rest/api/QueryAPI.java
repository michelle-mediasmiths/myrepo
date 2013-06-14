package com.mediasmiths.stdEvents.events.rest.api;

import com.mediasmiths.stdEvents.coreEntity.db.entity.AggregatedBMS;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AutoQC;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ManualQAEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
import org.joda.time.DateTime;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

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
	@Path("/BMSbydate")
	public List<AggregatedBMS> getAllBMSbyDate(final DateTime start, final DateTime end);
	
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
	@Path("/get_report-window_date")
	@Produces("text/plain")
	public List<EventEntity> getByEventNameWindowDateRange (String eventName, int max, DateTime start, DateTime end);
	
	@GET
	@Path("/get_report_window_date")
	@Produces("text/plain")
	public List<EventEntity> getEventsWindowDateRange (String namespace, String eventName, int max, DateTime start, DateTime end);

	@GET
	@Path("/OrdersByDate")
	List<OrderStatus> getOrdersInDateRange(DateTime start, DateTime end);

	@GET
	@Path("AutoQcByDate")
	List<AutoQC> getAutoQcInDateRange(DateTime start, DateTime end);
	
	public Title getTitleById(String id);
	
	public OrderStatus getOrderStatusById (String id);

	@GET
	@Path("ManualQAByDate")
	List<ManualQAEntity> getManualQAInDateRange(DateTime start, DateTime end);
}
