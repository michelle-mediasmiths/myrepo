package com.mediasmiths.foxtel.tc.guice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mediasmiths.foxtel.tc.service.TCRestService;
import com.mediasmiths.foxtel.tc.service.TCRestServiceImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class TCAdapterModule extends AbstractModule{

	private static final Logger log = Logger.getLogger(TCAdapterModule.class);
	
	@Override
	protected void configure() {
		bind(TCRestService.class).to(TCRestServiceImpl.class);
		RestResourceRegistry.register(TCRestService.class);
		
	}

}
