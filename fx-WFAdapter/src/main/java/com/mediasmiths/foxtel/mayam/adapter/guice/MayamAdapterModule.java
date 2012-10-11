package com.mediasmiths.foxtel.mayam.adapter.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.mayam.adapter.service.MayamAdapterRestService;
import com.mediasmiths.foxtel.mayam.adapter.service.MayamAdapterRestServiceImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class MayamAdapterModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(MayamAdapterRestService.class).to(MayamAdapterRestServiceImpl.class);
		RestResourceRegistry.register(MayamAdapterRestService.class);

	}

}
