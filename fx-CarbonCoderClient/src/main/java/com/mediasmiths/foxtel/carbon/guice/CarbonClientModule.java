package com.mediasmiths.foxtel.carbon.guice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mediasmiths.foxtel.carbon.CarbonClient;
import com.mediasmiths.foxtel.carbon.CarbonClientImpl;

public class CarbonClientModule extends AbstractModule
{

	private final static Logger log = Logger.getLogger(CarbonClientModule.class);
	
	@Override
	protected void configure()
	{
		bind(CarbonClient.class).to(CarbonClientImpl.class);
	}

	@Provides
	public Unmarshaller provideUnmarshaller() throws JAXBException {
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.carbon.jaxb");
		} catch (JAXBException e) {
			log.fatal("Could not create jaxb context", e);
			throw e;
		}
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jc.createUnmarshaller();
		} catch (JAXBException e) {
			log.fatal("Could not create unmarshaller", e);
			throw e;
		}
		return unmarshaller;
	}
}
