package com.mediasmiths.foxtel.agent.processing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.FilePickup;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 *
 * Send timing messages about
 * @author harmer
 *
 */
@Singleton
public class EventPickUpTimings 
{
	private final static Logger logger = Logger.getLogger(EventPickUpTimings.class);

	@Inject
	protected static EventAPI events;
	
	@Inject
	@Named("agent.events.namespace.pickupTimingsNS")
	protected String eventsNamespace;

    @Inject
    @Named("agent.events.pickupTimingType")
    protected String eventName;

	@Inject
    @Named("agent.events.timingPayloadMarshaller")
	protected Marshaller marshaller;
	
	@Inject
	@Named("service.events.enabled")
	protected boolean enabled = false;

	/**
	 * events notification
	 * 
	 * @param payload the timing data object.
	 */
	public void saveEvent(FilePickup payload)
	{
		if (enabled)
		{

			if (logger.isDebugEnabled()) logger.debug("saving event with name" + eventName);

			try
			{
				EventEntity event = new EventEntity();
				event.setEventName(eventName);
				event.setNamespace(eventsNamespace);

                StringWriter payloadText = new StringWriter();
                marshaller.marshal(payload, payloadText);

				event.setPayload(payloadText.toString());
				event.setTime(System.currentTimeMillis());
				events.saveReport(event);
			}
            catch (JAXBException e)
            {
                logger.error(String.format("error saving event %s, JAXB marshalling exception",eventName), e);
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
}
