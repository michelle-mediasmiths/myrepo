package com.mediasmiths.stdEvents.persistence.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.DatatypeConverter;

public class QueryAPIImpl implements QueryAPI
{
	@Inject
	protected Injector injector;

	@Inject
	protected EventEntityDao eventDao;

	private static final transient Logger logger = Logger.getLogger(QueryAPIImpl.class);

	@Transactional
	public List<EventEntity> getEvents(String namespace, String eventName)
	{

		List<EventEntity> events = eventDao.findUnique(namespace, eventName);
		return events;
	}

	@Transactional
	public List<EventEntity> getAllEvents()
	{
		List<EventEntity> events = eventDao.getAll();
		return events;
	}

	@Transactional
	public EventEntity getById(Long id)
	{
		EventEntity event = eventDao.getById(id);
		return event;
	}
	
	@Transactional
	public void deleteById (Long id)
	{
		eventDao.deleteById(id);
	}

	@Transactional
	public List<EventEntity> getByNamespace(String namespace)
	{
		List<EventEntity> events = eventDao.findByNamespace(namespace);
		//eventDao.printXML(events);
		return events;
	}
	
	@Transactional
	public List<EventEntity> getByNamespaceWindow(@PathParam("namespace") String namespace, @PathParam("max") int max)
	{
		List<EventEntity> events = eventDao.namespacePaginated(namespace, 0, max);
		List<EventEntity> all = new ArrayList<EventEntity>();
		int start = 0;
		while (events.size()==max) {
			events = eventDao.namespacePaginated(namespace, start, max);
			start = start + max;
			all.addAll(events);
			logger.info("start: " + start + " Size: " + events.size());
			logger.info("Results: " + events);
		}
		return all;
	}
	
	@Transactional
	public List<EventEntity> getByEventNameWindow(@PathParam("eventname") String eventname, @PathParam("max") int max)
	{
		List<EventEntity> events = eventDao.eventnamePaginated(eventname, 0, max);
		logger.info("List size: " + events.size());
		List<EventEntity> all = events;
		int start = 0;
		while (events.size()==max) {
			events = eventDao.eventnamePaginated(eventname, start, max);
			start = start + max;
			all.addAll(events);
			logger.info("start: " + start + " Size: " + events.size());
			logger.info("Results: " + events);
		}
		logger.info("List size (all): " + all.size());
		return all;
	}
	
	@Transactional
	public List<EventEntity> getEventsWindow(@PathParam("namespace")String namespace, @PathParam("eventname")String eventname, @PathParam("max")int max)
	{
		logger.info("max @ queryAPIImpl: " + max);
		List<EventEntity> events = eventDao.findUniquePaginated(namespace, eventname, 0, max);
		List<EventEntity> all = new ArrayList<EventEntity>();
		all.addAll(events);
		int start=0;
		while (events.size()==max) {
			events = eventDao.findUniquePaginated(namespace, eventname, start, max);
			start = start + max;
			all.addAll(events);
			logger.info("start: " + start + " Size: " + events.size());
			logger.info("Results: " + events);
		}
		return all;
	}

	@Transactional
	public List<EventEntity> getByEventName(String eventName)
	{
		List<EventEntity> events = eventDao.findByEventName(eventName);
		//eventDao.printXML(events);
		return events;
	}

	@Transactional                                     
	public List<EventEntity> getCompliance()
	{
		List<EventEntity> events = getAllEvents();
		List<EventEntity> compliance = new ArrayList<EventEntity>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			if (payload.contains("ParentMaterialID"))
			{
				compliance.add(event);
			}
		}
		return compliance;
	}

	@Transactional
	public String getAvCompletionTime(List<EventEntity> events)
	{
		double totalTime = 0;
		int number = 0;
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			if ((payload.contains("taskStart")) && (payload.contains("taskFinish")))
			{
				String startString = payload.substring(payload.indexOf("taskStart")+10, payload.indexOf("</taskStart"));
				String finishString = payload.substring(payload.indexOf("taskFinish")+11, payload.indexOf("</taskFinsh"));
				if ((! startString.isEmpty()) && (! finishString.isEmpty()))
				{
					//use date time format
					Calendar taskStart = DatatypeConverter.parseDateTime(startString);
					Calendar taskFinish = DatatypeConverter.parseDateTime(finishString);
					double diff = (taskFinish.getTimeInMillis()) - (taskStart.getTimeInMillis());
					logger.info("diff: " + diff);
					totalTime += diff;
					number ++;
				}
			}
			
		}
		double average = totalTime/number;
		int avInt = (int) Math.round(average);
		String formatted = millisToHHMMSS(avInt);
		logger.info("double average: " + average + " int average: " + avInt + " string: " + formatted);
		return formatted;
	}
	
	private String millisToHHMMSS(int millis)
	{
		int secs = millis /1000;
		
		int hours = secs / 3600,
			remainder = secs % 3600,
			minutes = remainder / 60,
			seconds = remainder % 60;

				return ( (hours < 10 ? "0" : "") + hours
				+ ":" + (minutes < 10 ? "0" : "") + minutes
				+ ":" + (seconds< 10 ? "0" : "") + seconds );
	}
}
