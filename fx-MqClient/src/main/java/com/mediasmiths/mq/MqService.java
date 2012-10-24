package com.mediasmiths.mq;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class MqService  implements ServletContextListener {

	MqListeners listeners = new MqListeners();
	Thread listenersThread = new Thread(listeners);

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		listenersThread.start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		listenersThread.interrupt();
	}
}
