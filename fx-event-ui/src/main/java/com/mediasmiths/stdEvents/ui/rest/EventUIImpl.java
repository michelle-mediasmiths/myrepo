package com.mediasmiths.stdEvents.ui.rest;

import com.google.inject.Inject;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.ui.rss.NotificationServiceAPI;
import org.apache.log4j.Logger;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

public class EventUIImpl implements EventUI
{
	@Inject 
	private ThymeleafTemplater templater;
	
	@Inject
	private QueryAPIImpl queryApi;
	
	@Inject
	private EventAPI eventApi;
	
	@Inject
	private NotificationServiceAPI notificationServiceApi;
	
	public static final transient Logger logger =  Logger.getLogger(EventUIImpl.class);
	
	@Transactional
	public void saveReport(EventEntity event)
	{
		eventApi.saveReport(event);
	}
	
	@Transactional
	public void muleDemo(@PathParam("path")String path)
	{
		logger.info(path);
	}
	
	@Transactional
	public String getIndex()
	{
		TemplateCall call = templater.template("index");
		return call.process();
	}

	@Transactional
	public String getEvents()
	{
		final TemplateCall call = templater.template("events");
		call.set("events", queryApi.getAllEvents());
		return call.process();
	}
	
	@Transactional
	public String deleteEvent(@QueryParam("id") Long id)
	{
		final TemplateCall call = templater.template("events");
		queryApi.deleteById(id);
		call.set("events", queryApi.getAllEvents());
		return call.process();
	}

	@Transactional
	public String getEvent(@QueryParam("id") Long id)
	{
		final TemplateCall call = templater.template("event");
		call.set("events", queryApi.getById(id));
		return call.process();
	}

	@Transactional
	public String getFeed()
	{
		TemplateCall call = templater.template("atom_rss_feed");
		call.set("feeds", notificationServiceApi.getFeedAsString());
		return call.process();
	}
}
