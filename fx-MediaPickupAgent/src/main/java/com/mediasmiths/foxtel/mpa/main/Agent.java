package com.mediasmiths.foxtel.mpa.main;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.mediasmiths.foxtel.mpa.MediaPickupAgent;
import com.mediasmiths.foxtel.mpa.guice.MediaPickupSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;

public final class Agent implements Runnable
{

	private static Logger logger = Logger.getLogger(Agent.class);

	/**
	 * @param args
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws JAXBException, SAXException, InterruptedException
	{

		new Agent().run();

	}

	@Override
	public void run()
	{
		logger.info("Agentcli starting up");

		final Injector injector = GuiceInjectorBootstrap.createInjector(new MediaPickupSetup());

		try
		{
			MediaPickupAgent mpa = injector.getInstance(MediaPickupAgent.class);
			mpa.run();
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
