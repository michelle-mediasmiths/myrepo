package com.mediasmiths.foxtel.mpa.guice;

import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.mpa.queue.MaterialFolderWatcher;
import com.mediasmiths.foxtel.mpa.validation.MediaPickupAgentConfigValidator;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;

public class MediaPickupModule extends AbstractModule {

	private static Logger logger = Logger
			.getLogger(MediaPickupModule.class);
	
	@Override
	protected void configure() {
		bind(DirectoryWatchingQueuer.class).to(MaterialFolderWatcher.class);
//		bind(MESSAGEPROCESSOR_LITERAL).to(MaterialExchangeProcessor.class);
		bind(ConfigValidator.class).to(MediaPickupAgentConfigValidator.class);
	}

	protected static final TypeLiteral<MessageProcessor<Material>> MESSAGEPROCESSOR_LITERAL =  new TypeLiteral<MessageProcessor<Material>>(){};

	
	@Provides
	public JAXBContext provideJAXBContext() throws JAXBException{
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.MaterialExchange");
		} catch (JAXBException e) {
			logger.fatal("Could not create jaxb context", e);
			throw e;
		}
		return jc;
	}
	
	
	@Provides
	public Unmarshaller provideUnmarshaller(JAXBContext jc) throws JAXBException {
		
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
	public Marshaller provideMarshaller(JAXBContext jc, @Named("schema.location") String schemaLocation) throws JAXBException, SAXException {
		
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
	@Named("ruzz.jaxb.context")
	public JAXBContext provideRuzzJaxBContext() throws JAXBException{
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.ruzz");
		} catch (JAXBException e) {
			logger.fatal("Could not create jaxb context", e);
			throw e;
		}
		return jc;
	}
	
	
	@Provides
	@Named("ruzz.unmarshaller")
	public Unmarshaller provideRuzzUnmarshaller(@Named("ruzz.jaxb.context")JAXBContext jc) throws JAXBException {
		
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
	@Named("ruzz.marshaller")
	public Marshaller provideRuzzMarshaller(@Named("ruzz.jaxb.context")JAXBContext jc, @Named("ruzz.schema.location") String schemaLocation) throws JAXBException, SAXException {
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
	@Named("ruzz.schema.validator") 
	public SchemaValidator ruzzSchemaValidator(@Named("ruzz.schema.location") String schemaLocation) throws SAXException{
		SchemaValidator sv = new SchemaValidator(schemaLocation);
		return sv;
	}

}
