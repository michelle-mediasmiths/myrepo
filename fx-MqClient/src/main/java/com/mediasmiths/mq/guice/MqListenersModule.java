package com.mediasmiths.mq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.MembersInjector;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.mq.MqListeners;
import com.mediasmiths.mq.transferqueue.UnmatchedTransferManager;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

import javax.xml.bind.Marshaller;

public class MqListenersModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		MembersInjector<EventService> membersInjector = getMembersInjector(EventService.class);
		// make the wfe types marshaler the default instance
		// the marshaler in events service is not named and we have multiple marshalers used for different schemas
		bind(JAXBSerialiser.class).toProvider(MarshallerProvider.class);

		bind(MqListeners.class).asEagerSingleton();
		bind(UnmatchedTransferManager.class).asEagerSingleton();
	}
}
