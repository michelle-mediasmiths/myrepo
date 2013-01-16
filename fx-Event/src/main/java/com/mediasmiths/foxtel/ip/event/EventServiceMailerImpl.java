package com.mediasmiths.foxtel.ip.event;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.event.email.EmailAgent;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;

/**
 *
 * An implementation of the EventService that sends requests to a remote mailer agent a request that an email notification
 * be sent if required
 *
 * Author Harmer
 */
public class EventServiceMailerImpl extends EventService
{
	@Inject(optional=true)
	@Named("service.events.mailagent")
	EmailAgent mailAgent = null;

	@Override
	public void saveEvent(final String eventName, final String payload)
	{
		if (enabled)
		{
			super.saveEvent(eventName, payload);

			if (mailAgent != null)
			{
				mailAgent.eventNotify(eventsNamespace, eventName, payload);
			}
			else
			{
				logger.error("No email agent defined...unable to register email request for event " + eventName);
			}
		}
		else
		{
			logger.info("did not save event '" + eventName + "' as events are disabled");
		}
	}

	@Override
	public void saveEvent(final String eventName, final Object payload)
	{
		if (enabled)
		{
			try
			{
				String sPayload = getSerialisationOf(payload);

				super.saveEvent(eventName, payload);

				if (mailAgent != null)
				{
					mailAgent.eventNotify(eventsNamespace, eventName, sPayload);
				}
				else
				{
					logger.error("No email agent defined...unable to register email request for event " + eventName);
				}
			}
			catch (JAXBException e)
			{
				logger.error(String.format("error saving event %s", eventName), e);
			}
			catch (UnsupportedEncodingException e)
			{
				logger.error(String.format("error saving event %s", eventName), e);
			}

		}
		else
		{
			logger.info("did not save event '" + eventName + "' as events are disabled");
		}
	}

}
