package com.mediasmiths.foxtel.agent.processing;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;

@Singleton
public class EventService
{

	private final static Logger logger = Logger.getLogger(EventService.class);

	@Inject
	protected EventAPI events;
	@Inject
	@Named("service.events.namespace")
	protected String eventsNamespace;
	@Inject
	protected Marshaller marshaller;
	@Inject
	@Named("service.events.enabled")
	protected boolean enabled;

	/**
	 * events notification
	 * 
	 * @param name
	 * @param payload
	 */
	public void saveEvent(String name, String payload)
	{
		if (enabled)
		{

			logger.debug("saving event with name" + name);

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
				logger.error(String.format("error saving event %s",name), re);
			}
		}
		else
		{
			logger.info("did not save event '" + name + "' as events are disabled");
		}

	}

	/**
	 * events notification
	 * 
	 * @param name
	 * @param payload
	 */
	public void saveEvent(String name, Object payload)
	{
		if (enabled)
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
				logger.error(String.format("error saving event %s",name), e);
			}
			catch (UnsupportedEncodingException e)
			{
				logger.error(String.format("error saving event %s",name), e);
			}
		}
		else
		{
			logger.info("did not save event '" + name + "' as events are disabled");
		}

	}

}
