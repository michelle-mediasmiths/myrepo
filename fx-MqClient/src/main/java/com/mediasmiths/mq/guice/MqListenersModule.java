package com.mediasmiths.mq.guice;

import javax.xml.bind.Marshaller;

import com.google.inject.AbstractModule;
import com.google.inject.MembersInjector;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.mq.MqListeners;

public class MqListenersModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		MembersInjector<EventService> membersInjector = getMembersInjector(EventService.class);
		// make the wfe types marshaler the default instance
		// the marshaler in events service is not named and we have multiple marshalers used for different schemas
		bind(Marshaller.class).toProvider(MarshallerProvider.class);
		
		bind(MqListeners.class).asEagerSingleton();
	}

}
