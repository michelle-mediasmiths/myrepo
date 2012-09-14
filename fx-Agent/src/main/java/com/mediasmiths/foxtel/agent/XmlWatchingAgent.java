package com.mediasmiths.foxtel.agent;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;

public abstract class XmlWatchingAgent<T> implements StoppableService {

	private static final long FIVE_SECONDS = 5000L;

	private static Logger logger = Logger.getLogger(XmlWatchingAgent.class);

	private final MessageProcessor<T> messageProcessor;
	private final Thread messageProcessorThread;

	private final DirectoryWatchingQueuer directoryWatcher;
	private final Thread directoryWatcherThread;
	
	private final List<Thread> threads = new ArrayList<Thread>();

	public XmlWatchingAgent(ConfigValidator configValidator, DirectoryWatchingQueuer directoryWatcher,
			MessageProcessor<T> messageProcessor,ShutdownManager shutdownManager) throws JAXBException {
		logger.trace("XmlWatchingAgent constructor enter");

		// directory watching
		this.directoryWatcher = directoryWatcher;
		this.directoryWatcherThread = new Thread(directoryWatcher);
		this.directoryWatcherThread.setName("DirectoryWatcher");
		registerThread(directoryWatcherThread);

		// message validation + processing
		this.messageProcessor = messageProcessor;
		this.messageProcessorThread = new Thread(messageProcessor);
		this.messageProcessorThread.setName("MessageProcessor");
		registerThread(messageProcessorThread);

		// register with shutdown manager
		shutdownManager.register(this);

		logger.trace("XmlWatchingAgent constructor return");
	}
	
	protected void registerThread(Thread t){
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
		// ask workers to stop nicely
		messageProcessor.stop();
		directoryWatcher.setContinueWatching(false);

		// wait for a while
		try {
			Thread.sleep(FIVE_SECONDS);
		} catch (InterruptedException e) {
			logger.info("Interrupted during shutdown", e);
		}

		// interrupt workers who may be blocked on a queue or some other wait
		// operation
		messageProcessorThread.interrupt();
		directoryWatcherThread.interrupt();
	}
}
