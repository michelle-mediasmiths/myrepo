package com.mediasmiths.mq.guice;

import java.net.URI;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;

public class MqListenersModule extends AbstractModule {

	@Override
	protected void configure() {
//		install(new MayamClientModule());		
	}

	@Provides
	protected EventAPI getEventService(
			@Named("service.events.api.endpoint") final URI endpoint,
			final JAXRSProxyClientFactory clientFactory)
	{
		EventAPI service = clientFactory.createClient(EventAPI.class, endpoint);

		return service;
	}	
}
