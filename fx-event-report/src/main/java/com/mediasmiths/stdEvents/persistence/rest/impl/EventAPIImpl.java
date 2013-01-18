package com.mediasmiths.stdEvents.persistence.rest.impl;

import com.google.inject.Inject;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.EventingEntity;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.EventingDao;
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
	protected EventEntityDao eventDao;

	private final EventingDao eventingDao;

	@Inject
	public EventAPIImpl(EventingDao eventingDao)
	{
		this.eventingDao = eventingDao;

	}


	private static final transient Logger logger = Logger.getLogger(EventAPIImpl.class);

	public boolean EVENTING_SWITCH = true;

	EventTypeMapper storageTypeMapper = new EventTypeMapper();


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

			// request a specialised db table handler for this event namespace, and if it exists store the event in that table
		EventMarshaller dao = storageTypeMapper.get(event.getNamespace());

		if (dao != null)
		{
			dao.save(event);
		}

		    // TRUE if we are recording new events in to a new event tqble.
		if (EVENTING_SWITCH)
		{
			if (logger.isTraceEnabled())
				logger.info("Saving to Eventing table");

			EventingEntity eventingEntity = new EventingEntity();

			if (logger.isTraceEnabled())
				logger.info("Created correctly");

			eventingEntity.setEventId(event.id);

			logger.info("Saving to Eventing table");

			eventingDao.save(eventingEntity);

			logger.info("Event saved");
		}
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
