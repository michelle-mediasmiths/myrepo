package com.mediasmiths.foxtel.tc.guice;


import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.foxtel.tc.rest.impl.TCRestServiceImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class TCAdapterModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(TCRestService.class).to(TCRestServiceImpl.class);
		RestResourceRegistry.register(TCRestService.class);
		bind(TCRestServiceImpl.class).asEagerSingleton();
	}

}
