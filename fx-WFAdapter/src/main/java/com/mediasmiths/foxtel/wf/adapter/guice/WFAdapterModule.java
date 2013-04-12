package com.mediasmiths.foxtel.wf.adapter.guice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.MembersInjector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.wf.adapter.service.WFAdapterRestService;
import com.mediasmiths.foxtel.wf.adapter.service.WFAdapterRestServiceImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

public class WFAdapterModule extends AbstractModule
{
	private static Logger logger = Logger.getLogger(WFAdapterModule.class);

	@Override
	protected void configure()
	{
		bind(WFAdapterRestService.class).to(WFAdapterRestServiceImpl.class);
		RestResourceRegistry.register(WFAdapterRestService.class);
		MembersInjector<EventService> membersInjector = getMembersInjector(EventService.class);
		// make the wfe types marshaler the default instance
		// the marshaler in events service is not named and we have multiple marshalers used for different schemas
		bind(JAXBSerialiser.class).toProvider(WfeSerialiserProvider.class);
	}

	@Provides
	@Named("outputruzz.context")
	JAXBContext provideRuzzJAXBContext() throws JAXBException
	{
		JAXBContext jc = null;
		try
		{
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.outputruzz");
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
	@Named("outputruzz.serialiser")
	JAXBSerialiser provideRUZZJAXBSerialiser(@Named("outputruzz.context") JAXBContext context)
	{

		return JAXBSerialiser.getInstance(context);

	}

	@Provides
	@Named("mex.context")
	JAXBContext provideMEXJAXBContext() throws JAXBException
	{
		JAXBContext jc = null;
		try
		{
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.mediaexchange");
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
	@Named("mex.serialiser")
	JAXBSerialiser provideMEXJAXBSerialiser(@Named("mex.context") JAXBContext context)
	{

		return JAXBSerialiser.getInstance(context);

	}
	
	@Provides
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

}
