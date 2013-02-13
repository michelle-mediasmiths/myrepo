package com.mediasmiths.mq.guice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

public class JaxModule extends AbstractModule
{

	private final static Logger logger = Logger.getLogger(JaxModule.class);

	@Override
	protected void configure()
	{

	}

	@Provides
	@Singleton
	@Named("wfe.marshaller")
	Marshaller provideWFEMarshaller(@Named("wfe.context") JAXBContext jc) throws JAXBException, SAXException
	{
		Marshaller marshaller = null;
		try
		{
			marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		}
		catch (JAXBException e)
		{
			logger.fatal("Could not create marshaller", e);
			throw e;
		}
		return marshaller;
	}

	@Provides
	@Singleton
	@Named("wfe.context")
	JAXBContext provideWFEJAXBContext() throws JAXBException
	{
		JAXBContext jc = null;
		try
		{
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.wf.adapter.model");
		}
		catch (JAXBException e)
		{
			logger.fatal("Could not create jaxb context", e);
			throw e;
		}
		return jc;
	}

	@Provides
	@Singleton
	@Named("wfe.serialiser")
	JAXBSerialiser provideWFEJAXBSerialiser(@Named("wfe.context") JAXBContext context)
	{

		return JAXBSerialiser.getInstance(context);

	}

	@Provides
	@Singleton
	@Named("complianceLogging.context")
	JAXBContext providecomplianceLoggingJAXBContext() throws JAXBException
	{
		JAXBContext jc = null;
		try
		{
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingMarker");
		}
		catch (JAXBException e)
		{
			logger.fatal("Could not create jaxb context", e);
			throw e;
		}
		return jc;
	}

	@Provides
	@Singleton
	@Named("complianceLogging.serialiser")
	JAXBSerialiser providecomplianceLoggingJAXBSerialiser(@Named("complianceLogging.context") JAXBContext context)
	{

		return JAXBSerialiser.getInstance(context);

	}
	
	@Provides
	@Singleton
	@Named("complianceLogging.marshaller")
	Marshaller provideCompLoggingMarshaller(@Named("complianceLogging.context") JAXBContext jc) throws JAXBException, SAXException
	{
		Marshaller marshaller = null;
		try
		{
			marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		}
		catch (JAXBException e)
		{
			logger.fatal("Could not create marshaller", e);
			throw e;
		}
		return marshaller;
	}

}
