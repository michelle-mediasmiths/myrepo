package com.mediasmiths.foxtel.placeholder.main;

import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.mediasmiths.foxtel.placeholder.PlaceholderAgent;
import com.mediasmiths.foxtel.placeholder.guice.PlaceholderAgentSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;

public class PlaceholderAgentMain {

	private static Logger logger = Logger.getLogger(PlaceholderAgentMain.class);

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
		
		
		final Injector injector = GuiceInjectorBootstrap.createInjector(new PlaceholderAgentSetup());

		try
		{
			PlaceholderAgent pm = injector.getInstance(PlaceholderAgent.class);
			pm.run();
		}
		finally
		{
			// Cleanly shutdown
			injector.getInstance(ShutdownManager.class).shutdown();
		}
				
	}

}
