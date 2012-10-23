package com.mediasmiths.foxtel.tc.guice;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.tc.service.TCRestService;
import com.mediasmiths.foxtel.tc.service.TCRestServiceImpl;
import com.mediasmiths.foxtel.tc.ui.TcUi;
import com.mediasmiths.foxtel.tc.ui.TcUiImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class TCAdapterModule extends AbstractModule{

	private static final Logger log = Logger.getLogger(TCAdapterModule.class);
	
	@Override
	protected void configure() {
		bind(TCRestService.class).to(TCRestServiceImpl.class);
		bind(TcUi.class).to(TcUiImpl.class);
		RestResourceRegistry.register(TCRestService.class);
		RestResourceRegistry.register(TcUi.class);
		
	}

}
