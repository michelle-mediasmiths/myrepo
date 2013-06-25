package com.mediasmiths.foxtel.ip.event;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.IPEvent;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import org.apache.log4j.Logger;

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
	@Named("service.events.namespace")
	protected String eventsNamespace;

	@Inject
	@Named("service.events.enabled")
	protected boolean enabled;

	private static final JAXBSerialiser jax_b = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.events");

	@Override
	public void saveEvent(String eventName, String payload, String namespace)
	{
		if (enabled)
		{

			if (logger.isDebugEnabled()) logger.debug("saving event with name " + eventName);

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

	public void saveEvent(String namespace, String eventName, IPEvent ipEvent)
	{
		try
		{
			saveEvent(eventName,  jax_b.serialise(ipEvent), namespace);
		}
		catch (Throwable e)
		{
			logger.error("Could not unmarshall event " + eventName, e);
		}
	}

}
