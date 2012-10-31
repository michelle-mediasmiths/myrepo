package com.mediasmiths.foxtel.placeholder.guice;

import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.processing.PlaceholderMessageProcessor;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.stdEvents.persistence.rest.api.EventAPI;

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
	JAXBContext provideJAXBContext() throws JAXBException{
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		} catch (JAXBException e) {
			logger.fatal("Could not create jaxb context", e);
			throw e;
		}
		return jc;
	}
	
	@Provides
	Unmarshaller provideUnmarshaller(JAXBContext jc) throws JAXBException {
		
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jc.createUnmarshaller();
		} catch (JAXBException e) {
			logger.fatal("Could not create unmarshaller", e);
			throw e;
		}
		return unmarshaller;
	}
	
	@Provides
	Marshaller provideMarshaller(JAXBContext jc, @Named("schema.location") String schemaLocation) throws JAXBException, SAXException {
		
		Marshaller marshaller = null;
		try {
			marshaller = jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			SchemaFactory factory = SchemaFactory
					.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schema = factory.newSchema(getClass().getClassLoader()
					.getResource(schemaLocation));
			marshaller.setSchema(schema);
			
		} catch (JAXBException e) {
			logger.fatal("Could not create marshaller", e);
			throw e;
		}
		return marshaller;
	}
	
	@Provides
	protected EventAPI getEventService(
			@Named("service.events.api.endpoint") final URI endpoint,
			final JAXRSProxyClientFactory clientFactory)
	{
		logger.info(String.format("events api endpoint set to %s", endpoint));
		EventAPI service = clientFactory.createClient(EventAPI.class, endpoint);

		return service;
	}
	

}
