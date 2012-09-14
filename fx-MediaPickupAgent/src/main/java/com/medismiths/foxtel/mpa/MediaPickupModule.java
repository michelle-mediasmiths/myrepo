package com.medismiths.foxtel.mpa;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;

public class MediaPickupModule extends AbstractModule {

	private static Logger logger = Logger
			.getLogger(MediaPickupModule.class);
	
	@Override
	protected void configure() {
		bind(MayamClient.class).to(MayamClientImpl.class);

	}


	@Provides
	Unmarshaller provideUnmarshaller() {
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.MaterialExchange");
		} catch (JAXBException e) {
			logger.fatal("Could not create jaxb context", e);
			System.exit(1);
		}
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jc.createUnmarshaller();
		} catch (JAXBException e) {
			logger.fatal("Could not create unmarshaller", e);
			System.exit(1);
		}
		return unmarshaller;
	}

}
