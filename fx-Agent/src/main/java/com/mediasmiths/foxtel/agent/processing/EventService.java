package com.mediasmiths.foxtel.agent.processing;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.persistence.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.rest.api.EventAPI;

@Singleton
public class EventService
{
	
	private final static Logger logger = Logger.getLogger(EventService.class);
	
	@Inject
	protected EventAPI events;
	@Inject @Named("service.events.namespace") 
	protected String eventsNamespace;
	@Inject
	protected Marshaller marshaller;
	 
/**
 * events notification
 * @param name
 * @param payload
 */
	public void saveEvent(String name, String payload)
	{
		try
		{
			EventEntity event = new EventEntity();
			event.setEventName(name);
			event.setNamespace(eventsNamespace);

			event.setPayload(payload);
			event.setTime(System.currentTimeMillis());
			events.saveReport(event);
		}
		catch (RuntimeException re)
		{
			logger.error("error saving event" + name, re);
		}

	}


/**
 * events notification
 * @param name
 * @param payload
 */
	
	public void saveEvent(String name, Object payload)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.marshal(payload, baos);
			String sPayload = baos.toString("UTF-8");
			saveEvent(name, sPayload);
		}
		catch (JAXBException e)
		{
			logger.error("error saving event" + name, e);
		}
		catch (UnsupportedEncodingException e)
		{
			logger.error("error saving event" + name, e);
		}

	}

}
