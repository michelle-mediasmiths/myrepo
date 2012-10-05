package com.mediasmiths.foxtel.tc.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.tc.service.TCRestService;
import com.mediasmiths.foxtel.tc.service.TCRestServiceImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class TCAdapterModule extends AbstractModule{

	@Override
	protected void configure() {
		bind(TCRestService.class).to(TCRestServiceImpl.class);
		RestResourceRegistry.register(TCRestService.class);
		
	}

}
