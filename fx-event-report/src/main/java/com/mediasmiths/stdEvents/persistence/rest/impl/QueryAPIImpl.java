package com.mediasmiths.stdEvents.persistence.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AutoQC;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ManualQAEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Purge;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.AutoQCDao;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.ManualQAEntityDAO;
import com.mediasmiths.stdEvents.persistence.db.dao.OrderDao;
import com.mediasmiths.stdEvents.persistence.db.dao.PurgeDao;
import com.mediasmiths.stdEvents.persistence.db.dao.TitleDao;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

public class QueryAPIImpl implements QueryAPI
{
	@Inject
	protected Injector injector;

	@Inject
	protected EventEntityDao eventDao;

	@Inject
	protected OrderDao orderDao;
	
	@Inject
	protected TitleDao titleDao;
	
	@Inject
	protected AutoQCDao autoQcDao;

	@Inject
	protected ManualQAEntityDAO manualQADao;

	@Inject
	protected PurgeDao purgeDao;
	
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
		return events;
	}

	@Transactional
	public List<EventEntity> getByEventNameWindowDateRange(
			String eventName,
			int max,
			DateTime start,
			DateTime end)
	{
		List<EventEntity> events = eventDao.eventnamePaginatedDate(eventName, 0, max, start, end);
		List<EventEntity> all = new ArrayList<EventEntity>();
		all.addAll(events);
		int startInt = 0;
		while (events.size()==max) {
			events = eventDao.eventnamePaginatedDate(eventName, startInt, max, start, end);
			startInt = startInt + max;
			all.addAll(events);
			logger.info("Results: " + events);
		}
		
		return all;
	}
	
	@Transactional
	public List<EventEntity> getEventsWindowDateRange(
			String namespace,
			String eventName,
			int max,
			DateTime start,
			DateTime end)
	{
		logger.debug(">>>getEventsWindowDateRange");
		logger.info("max @ queryAPIImpl: " + max);
		logger.debug("start: " + start + " end: " + end);
		List<EventEntity> events = eventDao.findUniquePaginatedDate(namespace, eventName, 0, max, start, end);
		List<EventEntity> all = new ArrayList<EventEntity>();
		all.addAll(events);
		int startInt=0;
		while (events.size()==max) {
			events = eventDao.findUniquePaginatedDate(namespace, eventName, startInt, max, start, end);
			startInt = startInt + max;
			all.addAll(events);
			logger.info("start: " + startInt + " Size: " + events.size());
			logger.info("Results: " + events);
		}
		logger.debug("<<<getEventsWindowDateRange");
		return all;
	}

	@Transactional
	public List<EventEntity> getByEventName(String eventName)
	{
		List<EventEntity> events = eventDao.findByEventName(eventName);
		//eventDao.printXML(events);
		return events;
	}

	@Override
	public List<OrderStatus> getOrdersInDateRange(DateTime start, DateTime end)
	{
		return orderDao.getOrdersInDateRange(start,end);
	}

	@Override 
	public Title getTitleById(String id)
	{
		return titleDao.getById(id);
	}

	@Override
	public OrderStatus getOrderStatusById(String id)
	{
		return orderDao.getById(id);
	}


	@Override
	public List<ManualQAEntity> getManualQAInDateRange(final DateTime start, final DateTime end)
	{
		return manualQADao.getManualQAInDateRange(start, end);
	}


	@Override
	public List<Purge> getPurgeEventsInDateRange(final DateTime start, final DateTime end)
	{
		return purgeDao.getPurgeInDateRange(start,end);
	}


	@Override
	@GET
	@Path("AutoQcByDate")
	public List<AutoQC> getAutoQcInDateRange(DateTime start, DateTime end)
	{
		return autoQcDao.getAutoQcInDateRange(start,end);
	}	
}
