package com.mediasmiths.foxtel.placeholder;

import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.mediasmiths.foxtel.placeholder.guice.PlaceholderManagementSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;

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
		
		
		final Injector injector = GuiceInjectorBootstrap.createInjector(new PlaceholderManagementSetup());

		try
		{
			PlaceHolderManager pm = injector.getInstance(PlaceHolderManager.class);
			pm.run();
		}
		finally
		{
			// Cleanly shutdown
			injector.getInstance(ShutdownManager.class).shutdown();
		}
				
	}

}
