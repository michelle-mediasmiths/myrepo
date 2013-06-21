package com.mediasmiths.mq;

import com.google.inject.Injector;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.serviceregistry.ApplicationContextNameRegistry;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class MqService  implements ServletContextListener {

	protected final static Logger log = Logger.getLogger(MqService.class);
	
	MqListeners listeners;
	Thread listenersThread;

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		log.trace("context initilized enter");
		ApplicationContextNameRegistry.setContextName("fx-MqClient");
		final Injector injector = GuiceInjectorBootstrap.createInjector();
		log.trace("injector created");
		
		listeners = injector.getInstance(MqListeners.class);
		listenersThread = new Thread(listeners);
		listenersThread.start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		listenersThread.interrupt();
	}
}
