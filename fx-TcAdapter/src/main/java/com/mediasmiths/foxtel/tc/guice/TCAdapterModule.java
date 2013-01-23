package com.mediasmiths.foxtel.tc.guice;


import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.tc.rest.impl.TCRestService;
import com.mediasmiths.foxtel.tc.rest.impl.TCRestServiceImpl;
import com.mediasmiths.foxtel.tc.ui.TcUi;
import com.mediasmiths.foxtel.tc.ui.TcUiImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class TCAdapterModule extends AbstractModule{

	@Override
	protected void configure() {
		bind(TCRestService.class).to(TCRestServiceImpl.class);
		bind(TcUi.class).to(TcUiImpl.class);
		RestResourceRegistry.register(TCRestService.class);
		RestResourceRegistry.register(TcUi.class);
		
	}

}
