package com.mediasmiths.foxtel.mpa.main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AgentContextListener implements ServletContextListener
{

	Agent agent = new Agent();
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
