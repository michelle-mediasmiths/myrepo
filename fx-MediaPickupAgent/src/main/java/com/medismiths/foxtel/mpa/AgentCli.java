package com.medismiths.foxtel.mpa;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;

public class AgentCli {

	private static Logger logger = Logger.getLogger(AgentCli.class);
	
	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws JAXBException, SAXException {

		logger.info("Agentcli starting up");
		
		
		final Injector injector = GuiceInjectorBootstrap.createInjector(new MediaPickupSetup());

		try
		{
			MediaPickupAgent mpa = injector.getInstance(MediaPickupAgent.class);
			mpa.start();
		}
		finally
		{
			// Cleanly shutdown
			injector.getInstance(ShutdownManager.class).shutdown();
		}
				
	}

}
