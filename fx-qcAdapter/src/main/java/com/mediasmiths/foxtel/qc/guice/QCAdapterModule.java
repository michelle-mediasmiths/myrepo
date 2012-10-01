package com.mediasmiths.foxtel.qc.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.qc.QCRestService;
import com.mediasmiths.foxtel.qc.QCRestServiceImpl;
import com.mediasmiths.std.guice.web.rest.RestResourceRegistry;


public class QCAdapterModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(QCRestService.class).to(QCRestServiceImpl.class);
		RestResourceRegistry.register(QCRestService.class);
	}

}
