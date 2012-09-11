package com.mediasmiths.foxtel.placeholder;

import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class PlaceHolderManagerCLI {

	private static Logger logger = Logger.getLogger(PlaceHolderManagerCLI.class);

	/**
	 * @param args
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws MalformedURLException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws JAXBException, SAXException,
			MalformedURLException, InterruptedException {

		logger.info("Placeholdermanger cli starting up");
		
		Injector injector = Guice.createInjector(new PlaceHolderMangementModule());
		PlaceHolderManager pm = injector.getInstance(PlaceHolderManager.class);
		pm.run();
		
	}

}
