package com.mediasmiths.foxtel.placeholder;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.placeholder.processing.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.receipt.ReceiptWriter;
import com.mediasmiths.foxtel.placeholder.validation.ConfigValidator;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;

public class PlaceHolderManager implements StoppableService{

	static Logger logger = Logger.getLogger(PlaceHolderManager.class);

	private final MessageProcessor messageProcessor;
	private final Thread messageProcessorThread;

	private final PlaceHolderMessageDirectoryWatcher directoryWatcher;
	private final Thread directoryWatcherThread;

	@Inject
	public PlaceHolderManager(MayamClient mc,MessageValidator validator, MessageProcessor processor,ReceiptWriter receiptWriter,PlaceHolderMessageDirectoryWatcher directoryWatcher, ShutdownManager shutdownManager, ConfigValidator configValidator) throws JAXBException,
			SAXException {

		logger.trace("Placeholdermanager constructor enter");

		//directory watching
		this.directoryWatcher = directoryWatcher;
		this.directoryWatcherThread = new Thread(directoryWatcher);

		//message validation + processing
		this.messageProcessor=processor;
		this.messageProcessorThread = new Thread(messageProcessor);
		
		//register with shutdown manager
		shutdownManager.register(this);
		
		logger.trace("Placeholdermanager constructor return");
		
	}

	public void run() throws InterruptedException{
		
		logger.debug("PlaceHolderManager run");
		
		logger.debug("starting directory watcher");
		directoryWatcherThread.start();
		logger.debug("starting message processor");
		messageProcessorThread.start();		
		
		logger.debug("Threads started");
		
		directoryWatcherThread.join();
		messageProcessorThread.join();
	}

	@Override
	public void shutdown() {
		//ask workers to stop nicely
		messageProcessor.stop();
		directoryWatcher.setContinueWatching(false);
		
		//wait for a while
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			logger.info("Interrupted during shutdown",e);
		}
		
		//interrupt workers who may be blocked on a queue or some other wait operation
		messageProcessorThread.interrupt();
		directoryWatcherThread.interrupt();
	}
}
