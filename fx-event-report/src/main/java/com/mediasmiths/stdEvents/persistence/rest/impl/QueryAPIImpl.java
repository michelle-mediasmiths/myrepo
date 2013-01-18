package com.mediasmiths.stdEvents.persistence.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
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
import com.mediasmiths.stdEvents.persistence.db.impl.IPEventDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.PlaceholderMessageDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.QCDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.TranscodeDaoImpl;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
	protected TranscodeDaoImpl transcodeDao;
	@Inject
	protected QCDaoImpl qcDao;
	@Inject
	protected DeliveryDaoImpl deliveryDao;

	private static final transient Logger logger = Logger.getLogger(QueryAPIImpl.class);

	Map<String, Class<? extends QueryDao<?>>> getDao = createQueryMap();

	private Map<String, Class<? extends QueryDao<?>>> createQueryMap()
	{
		Map<String, Class<? extends QueryDao<?>>> getDao = new HashMap<String, Class<? extends QueryDao<?>>>();
		getDao.put("placeholderMessage", PlaceholderMessageDaoImpl.class);
		getDao.put("contentPickup", ContentPickupDaoImpl.class);
		getDao.put("transcode", TranscodeDaoImpl.class);
		getDao.put("qc", QCDaoImpl.class);
		getDao.put("delivery", DeliveryDaoImpl.class);
		getDao.put("system", IPEventDaoImpl.class);
		return getDao;
	}

	@Transactional
	public List<?> getAll(String type)
	{
		logger.info("Getting all events of type " + type);
		Class<? extends QueryDao<?>> daoClass = getDao.get(type);
		if (daoClass != null)
		{
			logger.info("Matching dao for type found");
			QueryDao<?> dao = injector.getInstance(daoClass);
			List<?> results = dao.getAll();
			logger.info(results);
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
		logger.info("Finding event...");
		List<EventEntity> events = eventDao.findUnique(namespace, eventName);
		logger.info("Event found");
		return events;
	}

	@Transactional
	public List<EventEntity> getAllEvents()
	{
		logger.info("Getting all events...");
		List<EventEntity> events = eventDao.getAll();
		logger.info("Finished search");
		return events;
	}

	@Transactional
	public EventEntity getById(Long id)
	{
		logger.info("Getting event by id...");
		EventEntity event = eventDao.getById(id);
		return event;
	}
	
	@Transactional
	public void deleteById (Long id)
	{
		logger.info("Deleting event: " + id);
		eventDao.deleteById(id);
	}

	@Transactional
	public List<EventEntity> getByNamespace(String namespace)
	{
		logger.info("Getting event by namespace...");
		List<EventEntity> events = eventDao.findByNamespace(namespace);
		logger.info("Finished search");
		eventDao.printXML(events);
		return events;
	}

	@Transactional
	public List<EventEntity> getByEventName(String eventName)
	{
		logger.info("Getting event by eventName...");
		List<EventEntity> events = eventDao.findByEventName(eventName);
		logger.info("Finished search");
		eventDao.printXML(events);
		return events;
	}

	@Transactional
	public List<EventEntity> getByMedia(String namespace, String eventName, String media)
	{
		List<EventEntity> events = eventDao.findUnique(namespace, eventName);
		List<EventEntity> results = new ArrayList<EventEntity>();

		logger.info("finding media for events...");
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

					logger.info(requiredByDate.toString());

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
		logger.info("Getting titles not delivered...");

		List<EventEntity> created = getDelivered();
		List<String> titleId = new ArrayList<String>();
		
		List<EventEntity> delivered = getEvents("http://www.foxtel.com.au/ip/delivery", "Delivered");

		List<EventEntity> notDelivered = new ArrayList<EventEntity>();

		boolean done = false;

		logger.info("Getting titleIDs...");
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
		logger.info("Finished looking for not delivered");

		return notDelivered;
	}

	@Transactional
	public List<EventEntity> getProtected()
	{
		List<EventEntity> events = getEvents("http://www.foxtel.com.au/ip/bms", "CreateOrUpdateTitle");
		List<EventEntity> purgeProtect = new ArrayList<EventEntity>();

		logger.info("Finding purge protected BMS...");
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

		logger.info("Getting exporiting titles...");
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
		logger.info("Total: " + total);
		return total;
	}

	@Transactional
	public int getLength(List<EventEntity> events)
	{
		logger.info("Getting list length...");
		logger.info("List: " + events);
		int length = events.size();
		logger.info("Length is: " + length);
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
