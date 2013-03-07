package com.mediasmiths.foxtel.ip.mail.threadmanager;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;
import com.mediasmiths.std.threading.Daemon;
import com.mediasmiths.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class EventMapperThreadManager extends Daemon implements StoppableService
{
	public static final transient Logger logger = Logger.getLogger(EventMapperThreadManager.class);

	private final Timeout betweenChecks = new Timeout(60, TimeUnit.SECONDS);
	private final ReadAndProcessEventingTable readAndProcessEventingTable;
	private final DeleteItemsIntable deleteItemsIntable;

	@Inject
	@Named("mail.agent.cleartablesonstartup")
	public Boolean deleteOnStartup;

	@Inject
	public EventMapperThreadManager(
			ShutdownManager manager,
			ReadAndProcessEventingTable readAndProcessEventingTable,
			DeleteItemsIntable deleteItemsIntable)
	{
		this.deleteItemsIntable = deleteItemsIntable;
		this.readAndProcessEventingTable = readAndProcessEventingTable;
		this.startThread();
		manager.register(this);
	}

	@Override
	public void run()
	{
		logger.info("Thread service Called");

		logger.info("deleteOnStartup: " + deleteOnStartup);

		if (deleteOnStartup)
		{
			logger.info("Call to delete all items in eventing table");
			deleteItemsIntable.deleteEventing();
		}
		else
			logger.info("Items in eventing and event table not deleted");

		if (logger.isTraceEnabled())
			logger.info("Starting worker service running");

		while (isRunning())
		{
			try
			{
				readAndProcessEventingTable.processEventList();
			}
			catch (Throwable t)
			{
				if (logger.isTraceEnabled())
					logger.info("A major error has occured in workerservice", t);
				betweenChecks.sleep();
			}
			if (logger.isTraceEnabled())
				logger.info("Going to sleep");
			betweenChecks.sleep();
		}
	}

	public void shutdown()
	{
		this.stopThread();
	}
}
