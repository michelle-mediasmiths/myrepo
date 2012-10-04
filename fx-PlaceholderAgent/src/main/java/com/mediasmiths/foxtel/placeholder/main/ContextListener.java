package com.mediasmiths.foxtel.placeholder.main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener
{

	PlaceholderAgentMain agent = new PlaceholderAgentMain();
	Thread agentThread = new Thread(agent);

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		agentThread.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		agentThread.interrupt();
	}

}
