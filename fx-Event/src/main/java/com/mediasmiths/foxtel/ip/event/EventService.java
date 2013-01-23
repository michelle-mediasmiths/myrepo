package com.mediasmiths.foxtel.ip.event;



import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Implementation to the general purpose Eventing System.
 *
 * Author: Harmer.
 *
 */
public class EventService implements EventHandler
{

	protected final static Logger logger = Logger.getLogger(EventService.class);

	@Inject
	protected EventAPI events;

	@Inject
	protected Marshaller marshaller;

	@Inject
	@Named("service.events.namespace")
	protected String eventsNamespace;

	@Inject
	@Named("service.events.enabled")
	protected boolean enabled;

	@Override
	public void saveEvent(String eventName, String payload, String namespace)
	{
		if (enabled)
		{

			if (logger.isDebugEnabled()) logger.debug("saving event with name" + eventName);

			try
			{
				EventEntity event = new EventEntity();
				event.setEventName(eventName);
				event.setNamespace(namespace);

				event.setPayload(payload);
				event.setTime(System.currentTimeMillis());
				events.saveReport(event);
			}
			catch (RuntimeException re)
			{
				logger.error(String.format("error saving event %s",eventName), re);
			}
		}
		else
		{
			logger.info("did not save event '" + eventName + "' as events are disabled");
		}
	}
	

	@Override
	public void saveEvent(String eventName, String payload)
	{
		saveEvent(eventName, payload,eventsNamespace);
	}

	@Override
	public void saveEvent(String eventName, Object payload, String namespace)
	{
		if (enabled)
		{

			try
			{
 				saveEvent(eventName, getSerialisationOf(payload));
			}
			catch (JAXBException e)
			{
				logger.error(String.format("error saving event %s",eventName), e);
			}
			catch (UnsupportedEncodingException e)
			{
				logger.error(String.format("error saving event %s",eventName), e);
			}
		}
		else
		{
			logger.info("did not save event '" + eventName + "' as events are disabled");
		}
	}
	
	/**
	 * events notification
	 *
	 * @param eventName the eventname of the event to be notified
	 * @param payload the event specific content for the event
	 */
	@Override
	public void saveEvent(String eventName, Object payload)
	{
		saveEvent(eventName, payload,eventsNamespace);
	}

	/**
	 *
	 * @param payload the object to be serialised
	 * @return the string serialisation of the payload object using the default serialiser.
	 *
	 * @throws JAXBException
	 * @throws UnsupportedEncodingException
	 */
	protected String getSerialisationOf(Object payload) throws JAXBException, UnsupportedEncodingException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.marshal(payload, baos);
		return baos.toString("UTF-8");
	}
}
