package com.mediasmiths.foxtel.ip.event.guice;

import java.net.URI;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;

@Singleton
public class EventAPIProvider implements Provider<EventAPI>
{

	private final static Logger logger = Logger.getLogger(EventAPIProvider.class);
	private EventAPI service;

	@Inject
	public EventAPIProvider(@Named("service.events.api.endpoint") final URI endpoint, final JAXRSProxyClientFactory clientFactory)
	{
		logger.info(String.format("events api endpoint set to %s", endpoint));
		service = clientFactory.createClient(EventAPI.class, endpoint);

	}

	@Override
	public EventAPI get()
	{
		return service;
	}

}
