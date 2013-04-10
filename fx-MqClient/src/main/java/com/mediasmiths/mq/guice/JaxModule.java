package com.mediasmiths.mq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class JaxModule extends AbstractModule
{

	private final static Logger logger = Logger.getLogger(JaxModule.class);

	@Override
	protected void configure()
	{

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
	@Named("fxcommon.context")
	JAXBContext providefxcommonJAXBContext() throws JAXBException
	{
		JAXBContext jc = null;
		try
		{
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.ip.common.events");
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
	@Named("fxcommon.serialiser")
	JAXBSerialiser providefxcommonJAXBSerialiser(@Named("fxcommon.context") JAXBContext context)
	{
		return JAXBSerialiser.getInstance(context);
	}

	@Provides
	@Singleton
	@Named("placeholderManagement.context")
	JAXBContext provideplaceholderManagementJAXBContext() throws JAXBException
	{
		JAXBContext jc = null;
		try
		{
			jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
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
	@Named("placeholderManagement.serialiser")
	JAXBSerialiser providePHMJAXBSerialiser(@Named("placeholderManagement.context") JAXBContext context)
	{

		return JAXBSerialiser.getInstance(context);

	}

	/**
	 * TODO remove this - it WILL fail badly. replace with JAXBSerialiser
	 * @param jc
	 * @return
	 * @throws JAXBException
	 * @throws SAXException
	 */
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

}
