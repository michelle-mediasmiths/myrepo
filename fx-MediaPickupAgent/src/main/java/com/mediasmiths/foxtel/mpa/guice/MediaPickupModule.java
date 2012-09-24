package com.mediasmiths.foxtel.mpa.guice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;
import com.mediasmiths.foxtel.mpa.processing.MaterialExchangeProcessor;
import com.mediasmiths.foxtel.mpa.queue.MaterialFolderWatcher;
import com.mediasmiths.foxtel.mpa.validation.MediaPickupAgentConfigValidator;

public class MediaPickupModule extends AbstractModule {

	private static Logger logger = Logger
			.getLogger(MediaPickupModule.class);
	
	@Override
	protected void configure() {
		bind(MayamClient.class).to(MayamClientImpl.class);
		bind(DirectoryWatchingQueuer.class).to(MaterialFolderWatcher.class);
		bind(MESSAGEPROCESSOR_LITERAL).to(MaterialExchangeProcessor.class);
		bind(ConfigValidator.class).to(MediaPickupAgentConfigValidator.class);
	}

	protected static final TypeLiteral<MessageProcessor<Material>> MESSAGEPROCESSOR_LITERAL =  new TypeLiteral<MessageProcessor<Material>>(){};

	@Provides
	public Unmarshaller provideUnmarshaller() throws JAXBException {
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.MaterialExchange");
		} catch (JAXBException e) {
			logger.fatal("Could not create jaxb context", e);
			throw e;
		}
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jc.createUnmarshaller();
		} catch (JAXBException e) {
			logger.fatal("Could not create unmarshaller", e);
			throw e;
		}
		return unmarshaller;
	}

}
