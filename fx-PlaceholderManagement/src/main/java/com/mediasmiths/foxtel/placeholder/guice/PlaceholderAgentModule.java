package com.mediasmiths.foxtel.placeholder.guice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mediasmiths.foxtel.agent.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.processing.PlaceholderMessageProcessor;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;

public class PlaceholderAgentModule extends AbstractModule {

	private static Logger logger = Logger
			.getLogger(PlaceholderAgentModule.class);

	@Override
	protected void configure() {
		bind(MayamClient.class).to(MayamClientImpl.class);
		bind(MessageProcessor.class).to(PlaceholderMessageProcessor.class);
	}

	@Provides
	Unmarshaller provideUnmarshaller() {
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
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
