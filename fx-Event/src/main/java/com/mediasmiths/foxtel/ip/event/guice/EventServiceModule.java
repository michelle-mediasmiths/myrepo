package com.mediasmiths.foxtel.ip.event.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;

public class EventServiceModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(EventAPI.class).toProvider(EventAPIProvider.class);
	}

}
