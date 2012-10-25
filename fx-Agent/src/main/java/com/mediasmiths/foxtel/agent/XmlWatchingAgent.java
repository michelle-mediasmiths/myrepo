package com.mediasmiths.foxtel.agent;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;

public abstract class XmlWatchingAgent<T> implements StoppableService {

	private static Logger logger = Logger.getLogger(XmlWatchingAgent.class);

	private final Thread messageProcessorThread;
	private final Thread directoryWatcherThread;
	
	private final List<Thread> threads = new ArrayList<Thread>();

	@Inject
	public XmlWatchingAgent(ConfigValidator configValidator, DirectoryWatchingQueuer directoryWatcher,
			MessageProcessor<T> messageProcessor,ShutdownManager shutdownManager) throws JAXBException {
		logger.trace("XmlWatchingAgent constructor enter");

		//configvalidator is injected by guice, its constructor can fail. this prevents the application even starting if there is a problem with the config
		
		// directory watching
		this.directoryWatcherThread = new Thread(directoryWatcher);
		this.directoryWatcherThread.setName("DirectoryWatcher");
		registerThread(directoryWatcherThread);

		// message validation + processing

		this.messageProcessorThread = new Thread(messageProcessor);
		this.messageProcessorThread.setName("MessageProcessor");
		registerThread(messageProcessorThread);

		// register with shutdown manager
		shutdownManager.register(this);

		logger.trace("XmlWatchingAgent constructor return");
	}
	
	protected final void registerThread(Thread t){
		logger.debug(String.format("Registering thread %s", t.getName()));
		threads.add(t);
	}
	
	public void run() throws InterruptedException{
		
		logger.debug("XmlWatchingAgent run");
		
		for(Thread t : threads){
			logger.debug("starting "+t.getName());
			t.start();
		}
		
		logger.debug("Threads started");
		
		for(Thread t : threads){
			logger.debug("joining "+t.getName());
			t.join();
		}
		
	}

	@Override
	public void shutdown() {
		
		logger.info("shutting down");
		
		// interrupt worker threads
		messageProcessorThread.interrupt();
		directoryWatcherThread.interrupt();
	}
}
