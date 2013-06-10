package com.mediasmiths.foxtel.fs.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.fs.service.FSRestService;
import com.mediasmiths.foxtel.fs.service.FSRestServiceImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class FSAdapterModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(FSRestService.class).to(FSRestServiceImpl.class);
		RestResourceRegistry.register(FSRestService.class);
		bind(FSRestServiceImpl.class).asEagerSingleton();
	}

}
