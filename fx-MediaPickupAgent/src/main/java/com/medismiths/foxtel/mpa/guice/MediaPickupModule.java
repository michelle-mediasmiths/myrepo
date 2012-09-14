package com.medismiths.foxtel.mpa.guice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;
import com.medismiths.foxtel.mpa.processing.MaterialExchangeProcessor;
import com.medismiths.foxtel.mpa.queue.MaterialFolderWatcher;

public class MediaPickupModule extends AbstractModule {

	private static Logger logger = Logger
			.getLogger(MediaPickupModule.class);
	
	@Override
	protected void configure() {
		bind(MayamClient.class).to(MayamClientImpl.class);
		bind(DirectoryWatchingQueuer.class).to(MaterialFolderWatcher.class);
		bind(new TypeLiteral<MessageProcessor<Material>>(){}).to(MaterialExchangeProcessor.class);
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
