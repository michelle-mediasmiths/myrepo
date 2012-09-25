package com.mediasmiths.foxtel.mpa.main;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.mediasmiths.foxtel.mpa.MediaPickupAgent;
import com.mediasmiths.foxtel.mpa.guice.MediaPickupSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;

public final class AgentCli {

	private static Logger logger = Logger.getLogger(AgentCli.class);
	
	private AgentCli(){
		//hiding constructor, this class's only purpose is its main method
	}
	
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
