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
import com.mediasmiths.std.guice.serviceregistry.ApplicationContextNameRegistry;

public class PlaceholderAgentMain implements Runnable {

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
		new PlaceholderAgentMain().run();

	}
	
	
	@Override
	public void run()
	{
		logger.info("Placeholdermangercli starting up");

//		final Injector injector = GuiceInjectorBootstrap.createInjector(new PlaceholderAgentSetup());
		ApplicationContextNameRegistry.setContextName("fx-PlaceholderAgent");
		final Injector injector = GuiceInjectorBootstrap.createInjector();
		
		try
		{
			PlaceholderAgent pm = injector.getInstance(PlaceholderAgent.class);
			pm.run();
		}
		catch (InterruptedException e)
		{
			logger.info("Interrupted");
		}
		finally
		{
			// Cleanly shutdown
			injector.getInstance(ShutdownManager.class).shutdown();
		}

	}


}
