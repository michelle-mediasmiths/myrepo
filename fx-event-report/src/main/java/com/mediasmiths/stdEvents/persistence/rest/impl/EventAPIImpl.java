package com.mediasmiths.stdEvents.persistence.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.PathParam;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.EventingEntity;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.EventingDao;
import com.mediasmiths.stdEvents.persistence.db.impl.ContentPickupDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.DeliveryDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.IPEventDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.InfrastructureDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.ManualPurgeDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.PlaceholderMessageDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.QCDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.TranscodeDaoImpl;
import com.mediasmiths.stdEvents.persistence.rest.impl.eventmapping.EventTypeMapper;
import org.apache.log4j.Logger;

/**
 * An implementation of the EventAPI as a rest service that stored incoming events within a (hibernate fronted) database
 *
 * Author: Alison Boal.
 *
 */
public class EventAPIImpl implements EventAPI
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
	@Inject
	protected InfrastructureDaoImpl infrastructureDao;
	@Inject
	protected ManualPurgeDaoImpl manualPurgeDao;
	@Inject
	protected QueryAPI queryApi;


	private final EventingDao eventingDao;

	@Inject
	public EventAPIImpl(EventingDao eventingDao)
	{
		this.eventingDao = eventingDao;

	}

	@Inject
	private Set<EventMarshaller> translateDaoSet;
	
	private static final transient Logger logger = Logger.getLogger(EventAPIImpl.class);

	public boolean EVENTING_SWITCH = true;

	EventTypeMapper storageTypeMapper = new EventTypeMapper();
	
	Map<String, Class<? extends EventMarshaller>> storeFormat = createTranslateMap();
	
	private Map<String, Class<? extends EventMarshaller>> createTranslateMap()
	{
		Map<String, Class<? extends EventMarshaller>> storeFormat = new HashMap<String, Class<? extends EventMarshaller>>();
		storeFormat.put("http://www.foxtel.com.au/ip/bms", PlaceholderMessageDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/content", ContentPickupDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/tc", TranscodeDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/qc", QCDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/delivery", DeliveryDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/system", IPEventDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/infrastructure", InfrastructureDaoImpl.class);

		logger.info("\t" + storeFormat.toString());

		return storeFormat;
	}

	/**
	 * Takes in event details and saves them in the database
	 *
	 * @param event the details of the namespace/event name and payload
	 *
	 */
	@Transactional
	public void saveReport(EventEntity event)
	{
		if (logger.isTraceEnabled())
			logger.trace("Saving event...");

			// Save event to the all events table.
		eventDao.saveOrUpdate(event);
		Class<? extends EventMarshaller> daoClass = storeFormat.get(event.getNamespace());
		
		if (daoClass != null)
		{
			EventMarshaller dao = injector.getInstance(daoClass);
			dao.save(event);
		}
		logger.info("Saving to Eventing table");
		EventingEntity eventingEntity = new EventingEntity();
		logger.info("Created correctly");

		eventingEntity.setEventId(event.id);
		logger.info("Saveing to Eventing table");
		eventingDao.save(eventingEntity);

		logger.info("Event saved");
	}
	
	@Transactional
	public EventEntity getById(@PathParam("id") Long id)
	{
		logger.info("Getting event by id...");
		EventEntity event = eventDao.getById(id);
		return event;
	}

	@Transactional
	public List<EventEntity> getByNamespace(@PathParam("namespace") String namespace)
	{
		logger.info("Getting event by namespace...");
		List<EventEntity> events = eventDao.findByNamespace(namespace);
		logger.info("Finished search");
		eventDao.printXML(events);
		return events;
	}

	@Transactional
	public List<EventEntity> getByEventName(@PathParam("eventname") String eventName)
	{
		logger.info("Getting event by eventName...");
		List<EventEntity> events = eventDao.findByEventName(eventName);
		logger.info("Finished search");
		eventDao.printXML(events);
		return events;
	}


	@Transactional
	public void setEventingSwitch(boolean value)
	{
		EVENTING_SWITCH = value;
	}

	@Transactional
	public boolean getEventingSwitch()
	{
		return EVENTING_SWITCH;
	}

	@Transactional
	public void deleteEventing()
	{
		// eventDao.deleteEventing(new Long(370));
	}

}
