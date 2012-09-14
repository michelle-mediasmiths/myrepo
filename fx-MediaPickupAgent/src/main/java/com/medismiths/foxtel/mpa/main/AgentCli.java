package com.medismiths.foxtel.mpa.main;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.medismiths.foxtel.mpa.MediaPickupAgent;
import com.medismiths.foxtel.mpa.delivery.Importer;
import com.medismiths.foxtel.mpa.guice.MediaPickupSetup;

public class AgentCli {

	private static Logger logger = Logger.getLogger(AgentCli.class);
	
	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws SAXException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws JAXBException, SAXException, InterruptedException {

		logger.info("Agentcli starting up");
		
		
		final Injector injector = GuiceInjectorBootstrap.createInjector(new MediaPickupSetup());

		try
		{
			MediaPickupAgent mpa = injector.getInstance(MediaPickupAgent.class);
			mpa.run();
		}
		finally
		{
			// Cleanly shutdown
			injector.getInstance(ShutdownManager.class).shutdown();
		}
				
	}

}
