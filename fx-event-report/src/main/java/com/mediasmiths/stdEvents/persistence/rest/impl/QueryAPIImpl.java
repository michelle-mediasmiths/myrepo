package com.mediasmiths.stdEvents.persistence.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.ContentPickup;
import com.mediasmiths.stdEvents.events.db.entity.Delivery;
import com.mediasmiths.stdEvents.events.db.entity.PlaceholderMessage;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.QueryDao;
import com.mediasmiths.stdEvents.persistence.db.impl.ContentPickupDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.DeliveryDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.PlaceholderMessageDaoImpl;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class QueryAPIImpl implements QueryAPI
{
	@Inject
	protected Injector injector;

	@Inject
	protected EventEntityDao eventDao;
	@Inject
	protected PlaceholderMessageDaoImpl placeholderMessageDao;
	@Inject
	protected ContentPickupDaoImpl contentPickupDao;

	@Inject
	protected DeliveryDaoImpl deliveryDao;

	private static final transient Logger logger = Logger.getLogger(QueryAPIImpl.class);

	@Inject
	@Named("event.reporter.eventnamemap")
	Map<String, Class<? extends QueryDao<?>>> getDao;

	@Transactional
	public List<?> getAll(String type)
	{
		if (logger.isTraceEnabled()) logger.trace("Getting all events of type " + type);
		Class<? extends QueryDao<?>> daoClass = getDao.get(type);
		if (daoClass != null)
		{
			if (logger.isTraceEnabled()) logger.trace("Matching dao for type found");
			QueryDao<?> dao = injector.getInstance(daoClass);
			List<?> results = dao.getAll();
			if (logger.isTraceEnabled()) logger.trace(results);
			return results;
		}
		else
		{
			return null;
		}
	}

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
		eventDao.printXML(events);
		return events;
	}

	@Transactional
	public List<EventEntity> getByEventName(String eventName)
	{
		List<EventEntity> events = eventDao.findByEventName(eventName);
		eventDao.printXML(events);
		return events;
	}

	@Transactional
	public List<EventEntity> getByMedia(String namespace, String eventName, String media)
	{
		List<EventEntity> events = eventDao.findUnique(namespace, eventName);
		List<EventEntity> results = new ArrayList<EventEntity>();

		for (EventEntity event : events)
		{

			ContentPickup content = contentPickupDao.get(event);

			if (content.getMedia().equals(media))
			{
				results.add(event);
			}
			else
			{
				logger.info("media type not found");
			}
		}
		return results;
	}

	@Transactional
	public List<EventEntity> getOverdue()
	{
		List<EventEntity> notDelivered = getOutstanding();
		List<EventEntity> overdue = new ArrayList<EventEntity>();

		for (EventEntity event : notDelivered)
		{
			String payload = event.getPayload();
			if (payload.contains("RequiredBy"))
			{
				String requiredBy = payload.substring(payload.indexOf("RequiredBy") + 11, payload.indexOf("</RequiredBy"));

				try
				{
					DateFormat formatter;
					formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSz");
					Date requiredByDate;

					requiredByDate = (Date) formatter.parse(requiredBy);


					if (requiredByDate.after(new Date()))
					{
						overdue.add(event);
					}
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
			}
		}
		return overdue;
	}

	@Transactional
	public List<EventEntity> getDelivered()
	{		
		List<EventEntity> createTitle = getEvents("http://www.foxtel.com.au/ip/bms", "CreateOrUpdateTitle");
		List<EventEntity> addMaterial = getEvents("http://www.foxtel.com.au/ip/bms", "AddOrUpdateMaterial");
		List<EventEntity> addPackage = getEvents("http://www.foxtel.com.au/ip/bms", "AddOrUpdatePackage");

		List<EventEntity> delivered = new ArrayList<EventEntity>();
		delivered.addAll(createTitle);
		delivered.addAll(addMaterial);
		delivered.addAll(addPackage);
		
		return delivered;
	}

	@Transactional
	public List<EventEntity> getOutstanding()
	{

		List<EventEntity> created = getDelivered();
		List<String> titleId = new ArrayList<String>();
		
		List<EventEntity> delivered = getEvents("http://www.foxtel.com.au/ip/delivery", "Delivered");

		List<EventEntity> notDelivered = new ArrayList<EventEntity>();

		boolean done = false;

		for (EventEntity event : delivered)
		{

			Delivery delivery = deliveryDao.get(event);
			String titleIDString = delivery.getTitleID();
			titleId.add(titleIDString);
		}

		for (EventEntity event : created)
		{

			PlaceholderMessage message = placeholderMessageDao.get(event);

			String actions = message.getActions();
			if (!actions.contains("Invalid"))
			{
				String titleIDString = actions.substring(actions.indexOf("titleID="), actions.indexOf("titleID") + 10);

				for (String id : titleId)
				{

					if (titleIDString.equals(id))
					{
						done = true;
					}
				}
				if (!done)
				{
					notDelivered.add(event);
				}
				else
				{
					done = false;
				}
			}
		}

		return notDelivered;
	}

	@Transactional
	public List<EventEntity> getProtected()
	{
		List<EventEntity> events = getEvents("http://www.foxtel.com.au/ip/bms", "CreateOrUpdateTitle");
		List<EventEntity> purgeProtect = new ArrayList<EventEntity>();

		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			String purgeProtected = payload.substring(
					payload.indexOf("purgeProtect=") + 14,
					payload.indexOf("\">", payload.indexOf("purgeProtect=")));
			if (purgeProtected.equals("true"))
			{
				purgeProtect.add(event);
			}
		}
		return purgeProtect;
	}

	@Transactional
	public List<EventEntity> getExpiring()
	{
		List<EventEntity> events = getEvents("http://www.foxtel.com.au/ip/bms", "CreateOrUpdateTitle");

		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			String endDate = payload.substring(
					payload.indexOf("endDate=") + 9,
					payload.indexOf("\"/>", payload.indexOf("endDate")));
		}
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
	public int getTotal(List<EventEntity> events)
	{
		int total = 0;
		for (EventEntity event : events)
		{
			total += 1;
		}
		return total;
	}

	@Transactional
	public int getLength(List<EventEntity> events)
	{

		int length = events.size();
		return length;
	}

	@Transactional
	public List<String> getFormat(List<EventEntity> events)
	{
		List<String> formats = new ArrayList<String>();
		for (EventEntity event : events)
		{
			ContentPickup content = contentPickupDao.get(event);
			formats.add(content.getFormat());
		}
		return formats;
	}
}
