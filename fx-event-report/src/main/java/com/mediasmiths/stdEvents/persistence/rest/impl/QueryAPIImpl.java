package com.mediasmiths.stdEvents.persistence.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.ContentPickup;
import com.mediasmiths.stdEvents.events.db.entity.DeliveryDetails;
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
		//eventDao.printXML(events);
		return events;
	}

	@Transactional
	public List<EventEntity> getByEventName(String eventName)
	{
		List<EventEntity> events = eventDao.findByEventName(eventName);
		//eventDao.printXML(events);
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

			DeliveryDetails delivery = deliveryDao.get(event);
			String titleIDString = delivery.getMasterId();
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
	
	@Transactional
	public List<EventEntity> getTotalQAd()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional
	public List<EventEntity> getTotalFailedQA()
	{
		List<EventEntity> events = getByNamespace("http://www.foxtel.com.au/ip/qc");
		List<EventEntity> failedQA = new ArrayList<EventEntity>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			String qcStatus = payload.substring(payload.indexOf("QCStatus")+9, payload.indexOf("</QCStatus"));
			if (qcStatus.equals("QCFail(Overridden)"))
				failedQA.add(event);
		}
		return failedQA;
	}

	@Transactional
	public List<EventEntity> getTotalQCd()
	{
		List<EventEntity> events = getByNamespace("http://www.foxtel.com.au/ip/qc");
		List<EventEntity> qcd = new ArrayList<EventEntity>();
		for (EventEntity event : events) {
			String str = event.getPayload();
			if (str.contains("QCStatus")) {
				String qcStatus = str.substring(str.indexOf("QCStatus")+9, str.indexOf("</QCStatus"));
				if (qcStatus.equals("QCPass"))
					qcd.add(event);
			}	
		}
		return qcd;
	}

	@Transactional
	public List<EventEntity> getFailedQc()
	{
		List<EventEntity> events = getByNamespace("http://www.foxtel.com.au/ip/qc");
		List<EventEntity> failed = new ArrayList<EventEntity>();
		for (EventEntity event : events) {
			String str = event.getPayload();
			if (str.contains("QCStatus")) {
				String qcStatus = str.substring(str.indexOf("QCStatus")+9, str.indexOf("</QCStatus"));
				if ((qcStatus.equals("QCFail")) || (qcStatus.equals("QCFail(Overridden")))
					failed.add(event);
			}	
		}
		return failed;
	}

	@Transactional
	public List<EventEntity> getOperatorOverridden()
	{
		List<EventEntity> events = getByNamespace("http://www.foxtel.com.au/ip/qc");
		List<EventEntity> overridden = new ArrayList<EventEntity>();
		for (EventEntity event : events) {
			String str = event.getPayload();
			if (str.contains("QCStatus")) {
				String qcStatus = str.substring(str.indexOf("QCStatus")+9, str.indexOf("</QCStatus"));
				if (qcStatus.equals("QCFail(Overridden"))
					overridden.add(event);
			}	
		}
		return overridden;
	}

	@Transactional
	public List<EventEntity> getExpiringPurged()
	{
		List<EventEntity> events = getEvents("http://www.foxtel.com.au/ip/qc", "ManualPurge");
		logger.info("Events: " + events);
		List<EventEntity> purged = new ArrayList<EventEntity>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			if (payload.contains("DeletedIn")) {
				String deletedIn = payload.substring(payload.indexOf("DeletedIn")+10, payload.indexOf("</DeletedIn"));
				int deleted = Integer.parseInt(deletedIn);
				logger.info("Deleted in: " + deleted);
				if (deleted < 4)
					purged.add(event);
			}
		}
		logger.info(purged);
		return purged;
	}

	@Transactional
	public List<EventEntity> getPurgeProtected()
	{
		List<EventEntity> events = getEvents("http://www.foxtel.com.au/ip/qc", "ManualPurge");
		List<EventEntity> purged = new ArrayList<EventEntity>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			if (payload.contains("ProtectedStatus")) {
				String protectedStatus = payload.substring(payload.indexOf("ProtectedStatus")+16, payload.indexOf("</ProtectedStatus"));
				if (protectedStatus.equals("true"))
					purged.add(event);
			}
		}
		return purged;
	}

	@Transactional
	public List<EventEntity> getPurgePosponed()
	{
		List<EventEntity> events = getEvents("http://www.foxtel.com.au/ip/qc", "ManualPurge");
		List<EventEntity> purged = new ArrayList<EventEntity>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			if (payload.contains("ExtendedStatus")) {
				String extendedStatus = payload.substring(payload.indexOf("ExtendedStatus")+15, payload.indexOf("</ExtendedStatus"));
				if (extendedStatus.equals("true"))
					purged.add(event);
			}
		}
		return purged;
	}

	@Transactional
	public List<EventEntity> getTotalPurged()
	{
		List<EventEntity> title = getEvents("http://www.foxtel.com.au/ip/bms", "PurgeTitle");
		List<EventEntity> material = getEvents("http://www.foxtel.com.au/ip/bms", "DeleteMaterial");
		List<EventEntity> pack = getEvents("http://www.foxtel.com.au/ip/bms", "DeletePackage");
		List<EventEntity> manual = getEvents("http://www.foxtel.com.au/ip/bms", "MaunalPurge");
		List<EventEntity> purged = new ArrayList<EventEntity>();
		purged.addAll(title);
		purged.addAll(material);
		purged.addAll(pack);
		purged.addAll(manual);
		return purged;
	}
}
