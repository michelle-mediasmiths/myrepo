package com.mediasmiths.foxtel.placeholder.guice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.mayam.wf.attributes.server.AttributeMapMapper;
import com.mayam.wf.attributes.server.JacksonAttributeMapMapperImpl;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.placeholder.processing.PlaceholderMessageProcessor;
import com.mediasmiths.mayam.AlertImpl;
import com.mediasmiths.mayam.AlertInterface;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;
import com.mediasmiths.mayam.guice.MayamClientModule;

public class PlaceholderAgentModule extends AbstractModule {

	private static Logger logger = Logger
			.getLogger(PlaceholderAgentModule.class);

	@Override
	protected void configure() {
		install(new MayamClientModule());
		bind(PLACEHOLDERPROCESSOR_LITERAL).to(PlaceholderMessageProcessor.class);
		
	}
	protected static final TypeLiteral<MessageProcessor<PlaceholderMessage>> PLACEHOLDERPROCESSOR_LITERAL =  new TypeLiteral<MessageProcessor<PlaceholderMessage>>(){};
	
	@Provides
	Unmarshaller provideUnmarshaller() throws JAXBException {
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
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
