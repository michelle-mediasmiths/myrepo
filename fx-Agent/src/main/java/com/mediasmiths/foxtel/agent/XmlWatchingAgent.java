package com.mediasmiths.foxtel.agent;


import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;

public abstract class XmlWatchingAgent<T> implements StoppableService
{

	private static Logger logger = Logger.getLogger(XmlWatchingAgent.class);

	private final ShutdownManager shutDownManager;
	
	@Inject
	public XmlWatchingAgent(
			ConfigValidator configValidator,
			MessageProcessor messageProcessor,
			ShutdownManager shutdownManager) throws JAXBException
	{
		logger.trace("XmlWatchingAgent constructor enter");

		// configvalidator is injected by guice, its constructor can fail. this prevents the application even starting if there is a problem with the config
		this.shutDownManager = shutdownManager;
		// register with shutdown manager		
		shutdownManager.register(this);

		// message validation + processing
		addMessageProcessor(messageProcessor);

		logger.trace("XmlWatchingAgent constructor return");
	}
	
	public void addMessageProcessor(MessageProcessor messageProcessor){
		messageProcessor.startThread();
		registerService(messageProcessor);
	}
	
	public void registerService(StoppableService service){
		shutDownManager.register(service);
	}

	@Override
	public void shutdown()
	{
		logger.info("shutting down");
	}
}
