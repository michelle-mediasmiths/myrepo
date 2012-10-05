package com.mediasmiths.foxtel.carbonwfs.guice;

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import com.rhozet.rhozet_services_iwfcjmservices.IWfcJmServices;

import static com.mediasmiths.foxtel.carbonwfs.WfsClientConfig.WFS_ENDPOINT;;

public class WfsClientModule extends AbstractModule
{

	private static final Logger log = Logger.getLogger(WfsClientModule.class);

	@Override
	protected void configure()
	{

	}

	@Provides
	Unmarshaller provideUnmarshaller() throws JAXBException
	{
		JAXBContext jc = null;
		try
		{
			jc = JAXBContext.newInstance("com.rhozet.rhozet_services");
		}
		catch (JAXBException e)
		{
			log.fatal("Could not create jaxb context", e);
			throw e;
		}
		Unmarshaller unmarshaller = null;
		try
		{
			unmarshaller = jc.createUnmarshaller();
		}
		catch (JAXBException e)
		{
			log.fatal("Could not create unmarshaller", e);
			throw e;
		}
		return unmarshaller;
	}

	@Singleton
	@Provides
	public IWfcJmServices getServices(@Named(WFS_ENDPOINT) String location)
	{

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.getInInterceptors().add(new LoggingInInterceptor());
		factory.getOutInterceptors().add(new LoggingOutInterceptor());
		factory.setServiceClass(IWfcJmServices.class);
		factory.setAddress(location);
		IWfcJmServices services = (IWfcJmServices) factory.create();
		return services;

	}
}
