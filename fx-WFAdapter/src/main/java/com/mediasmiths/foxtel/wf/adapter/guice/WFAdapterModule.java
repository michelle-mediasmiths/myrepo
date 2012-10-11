package com.mediasmiths.foxtel.wf.adapter.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.wf.adapter.service.WFAdapterRestService;
import com.mediasmiths.foxtel.wf.adapter.service.WFAdapterRestServiceImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class WFAdapterModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(WFAdapterRestService.class).to(WFAdapterRestServiceImpl.class);
		RestResourceRegistry.register(WFAdapterRestService.class);

	}

}
