package com.mediasmiths.foxtel.wf.adapter.guice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.wf.adapter.service.WFAdapterRestService;
import com.mediasmiths.foxtel.wf.adapter.service.WFAdapterRestServiceImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class WFAdapterModule extends AbstractModule
{
	private static Logger logger = Logger
			.getLogger(WFAdapterModule.class);
	
	@Override
	protected void configure()
	{
		bind(WFAdapterRestService.class).to(WFAdapterRestServiceImpl.class);
		RestResourceRegistry.register(WFAdapterRestService.class);

	}
	

	@Provides
	@Named("mex.marshaller")
	Marshaller provideMexMarshaller(@Named("mex.context") JAXBContext jc)
			throws JAXBException,
			SAXException
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
	@Named("ruzz.marshaller")
	Marshaller provideRuzzMarshaller(@Named("ruzz.context") JAXBContext jc)
			throws JAXBException,
			SAXException
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
	@Named("mex.context")
	JAXBContext provideMEXJAXBContext() throws JAXBException{
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.mediaexchange");			
			} catch (JAXBException e) {
				logger.fatal("Could not create jaxb context", e);
				throw e;
			}
		return jc;
		}
	
	@Provides
	@Named("ruzz.context")
	JAXBContext provideRuzzJAXBContext() throws JAXBException{
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.ruzz");			
			} catch (JAXBException e) {
				logger.fatal("Could not create jaxb context", e);
				throw e;
			}
		return jc;
		}
}
