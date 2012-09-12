package com.mediasmiths.foxtel.placeholder;

import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;

public class PlaceHolderMangementModule extends AbstractModule {

	private static Logger logger = Logger
			.getLogger(PlaceHolderMangementModule.class);

	@Override
	protected void configure() {
		bind(MayamClient.class).to(MayamClientImpl.class);

//		try {
//			Properties properties = new Properties();
//			properties.load(this.getClass().getClassLoader()
//					.getResourceAsStream("placeholdermanagement.properties"));
//			Names.bindProperties(binder(), properties);
//		} catch (IOException ex) {
//			logger.fatal("could not load properties", ex);
//			System.exit(1);
//		}

	}

	@Provides
	Unmarshaller provideUnmarshaller() {
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		} catch (JAXBException e) {
			logger.fatal("Could not create jaxb context", e);
			System.exit(1);
		}
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jc.createUnmarshaller();
		} catch (JAXBException e) {
			logger.fatal("Could not create unmarshaller", e);
			System.exit(1);
		}
		return unmarshaller;
	}

}
