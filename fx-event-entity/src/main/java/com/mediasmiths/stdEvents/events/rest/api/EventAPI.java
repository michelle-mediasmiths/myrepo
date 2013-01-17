package com.mediasmiths.stdEvents.events.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

/**
 * EventAPI is now used just to save the events
 * The methods that were now used to query the db are now in QueryAPI
 * Implemented by EventAPIImpl
 * @author alisonboal
 *
 */
@Path("/event_service")
public interface EventAPI
{

	@POST
	@Path("/save_report")
	@Consumes("application/xml")
	public void saveReport(EventEntity event);
	
	@GET
	@Path("/get_eventing_switch")
	@Produces("text/plain")
	public boolean getEventingSwitch();
	
	@GET
	@Path("/set_eventing_switch")
	@Produces("text/plain")
	@Consumes("text/plain")
	public void setEventingSwitch(boolean value);
	
	@DELETE
	@Path("/delete_eventing")
	public void deleteEventing();

	
}
