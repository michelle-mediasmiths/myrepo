package com.mediasmiths.stdEvents.persistence.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.EventingEntity;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.AutoQCDao;
import com.mediasmiths.stdEvents.persistence.db.dao.ComplianceLoggingDao;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.EventingDao;
import com.mediasmiths.stdEvents.persistence.db.dao.ExtendedPublishingDao;
import com.mediasmiths.stdEvents.persistence.db.dao.ManualQAEntityDAO;
import com.mediasmiths.stdEvents.persistence.db.dao.OrderDao;
import com.mediasmiths.stdEvents.persistence.db.dao.PurgeDao;
import com.mediasmiths.stdEvents.persistence.db.dao.TitleDao;
import com.mediasmiths.stdEvents.persistence.db.dao.TranscodeJobDao;
import com.mediasmiths.stdEvents.persistence.rest.impl.eventmapping.EventTypeMapper;
import org.apache.log4j.Logger;

import javax.ws.rs.PathParam;

/**
 * An implementation of the EventAPI as a rest service that stored incoming events within a (hibernate fronted) database
 * <p/>
 * Author: Alison Boal.
 */
public class EventAPIImpl implements EventAPI
{
	@Inject
	protected Injector injector;

	@Inject
	protected EventEntityDao eventDao;

	@Inject
	protected TitleDao titleDao;

	@Inject
	protected OrderDao orderDao;

	@Inject
	protected AutoQCDao autoQcDao;

	@Inject
	protected ManualQAEntityDAO manualQaDao;

	@Inject
	protected PurgeDao purgeDao;

	@Inject
	protected ExtendedPublishingDao extendedPublishingDao;

	@Inject
	protected ComplianceLoggingDao complianceLoggingDao;

	@Inject
	protected TranscodeJobDao transcodeJobDao;


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
	 * @param event
	 * 		the details of the namespace/event name and payload
	 */
	@Transactional
	public void saveReport(EventEntity event)
	{
		if (logger.isTraceEnabled())
			logger.trace("Saving event...");

		// Save event to the all events table.
		logger.info("Saving to Event table EVENT_NAME: " + event.getEventName() + " NAMESPACE: " + event.getNamespace());


		eventDao.saveOrUpdate(event);

		EventingEntity eventingEntity = new EventingEntity();
		logger.info("Created correctly");

		eventingEntity.setEventId(event.id);
		logger.info("Saving to Eventing table");
		eventingDao.save(eventingEntity);

		logger.info("Saving event information for reporting");

		if ((event.getEventName().equals(EventNames.CREATEOR_UPDATE_TITLE)))
		{
			createOrUpdateTitle(event);
		}

		if ((event.getEventName().equals(EventNames.ADD_OR_UPDATE_MATERIAL)))
		{
			addOrUpdateMaterial(event);
		}

		if ((event.getEventName().equals(EventNames.AUTO_QC_REPORT)))
		{
			autoQcReport(event);
		}

		if ((event.getEventName().equals(EventNames.MANUAL_QA)))
		{
			manualQaEvent(event);
		}

		if ((event.getEventName().equals(EventNames.PURGE_EVENT_NOTIFICATION)))
		{
			purgeEventNotification(event);
		}

		if ((event.getEventName().equals(EventNames.EXTENDED_PUBLISHING_TASK_EVENT)))
		{
			extendedPublishingEvent(event);
		}

		if ((event.getEventName().equals(EventNames.COMPLIANCE_LOGGING_TASK_EVENT)))
		{
			complianceTaskEvent(event);
		}


		if((event.getEventName().equals(EventNames.TRANSCODE_REPORT_DATA))){
			transcodeReportData(event);
		}


		logger.info("Event saved");
	}


	private void transcodeReportData(final EventEntity event)
	{
		transcodeJobDao.transcodeReportMessage(event);
	}


	private void complianceTaskEvent(final EventEntity event)
	{
		complianceLoggingDao.complianceEvent(event);
	}


	private void extendedPublishingEvent(final EventEntity event)
	{
		extendedPublishingDao.extendedPublishingEvent(event);
	}


	private void purgeEventNotification(final EventEntity event)
	{
		purgeDao.purgeMessage(event);
	}


	private void manualQaEvent(final EventEntity event)
	{
		manualQaDao.manualQCMessage(event);
	}


	private void autoQcReport(EventEntity event)
	{
		autoQcDao.autoQCMessage(event);
	}


	private void addOrUpdateMaterial(EventEntity event)
	{
		orderDao.addOrUpdateMaterial(event);
	}


	private void createOrUpdateTitle(EventEntity event)
	{
		titleDao.createOrUpdateTitle(event);
	}


	@Transactional
	public EventEntity getById(@PathParam("id") Long id)
	{
		logger.info("Getting event by id...");
		EventEntity event = eventDao.getById(id);
		return event;
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
