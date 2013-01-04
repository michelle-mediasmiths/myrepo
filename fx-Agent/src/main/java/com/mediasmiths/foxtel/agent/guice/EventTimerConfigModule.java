package com.mediasmiths.foxtel.agent.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.processing.EventPickUpTimings;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class EventTimerConfigModule extends AbstractModule
{

	private static Logger logger = Logger.getLogger(EventTimerConfigModule.class);


	@Override
	protected void configure()
	{
	}

	@Provides
	@Named("FX-Common.events.JAXBContext")
	JAXBContext providesJAXBContext () throws JAXBException
	{
		try
		{
			return JAXBContext.newInstance("com.mediasmiths.foxtel.ip.common.events");
		}
		catch (JAXBException e)
		{
			logger.fatal("Could not create jaxb context", e);
			throw e;
		}
	}

	@Provides
	@Named("agent.events.timingPayloadMarshaller")
	Marshaller provideMarshaller(@Named("FX-Common.events.JAXBContext")JAXBContext jc,
	                             @Named("schema.location.timingSchema")String schemaLocation) throws JAXBException, SAXException
	{
		try {
			Marshaller marshaller = jc.createMarshaller();

			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

			Schema schema = factory.newSchema(getClass().getClassLoader().getResource(schemaLocation));

			marshaller.setSchema(schema);

			return marshaller;
		} catch (JAXBException e)
		{
			logger.fatal("Could not create marshaller", e);
			throw e;
		}
		catch (SAXException e)
		{
			logger.fatal("Could not create marshaller (Schema Location issue)", e);
			throw e;
		}
	}

	@Provides
	@Named("agent.events.pickUpTimer")
	EventPickUpTimings pickUpEventTimer()
	{
		return new EventPickUpTimings();
	}

}
