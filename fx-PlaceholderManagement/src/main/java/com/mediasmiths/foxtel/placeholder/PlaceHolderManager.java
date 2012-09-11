package com.mediasmiths.foxtel.placeholder;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.placeholder.processing.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.receipt.ReceiptWriter;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;

public class PlaceHolderManager {

	static Logger logger = Logger.getLogger(PlaceHolderManager.class);

	private final MessageProcessor messageProcessor;
	private final Thread messageProcessorThread;

	private final PlaceHolderMessageDirectoryWatcher directoryWatcher;
	private final Thread directoryWatcherThread;

	@Inject
	public PlaceHolderManager(MayamClient mc,MessageValidator validator, MessageProcessor processor,ReceiptWriter receiptWriter,PlaceHolderMessageDirectoryWatcher directoryWatcher) throws JAXBException,
			SAXException {

		logger.trace("Placeholdermanager constructor enter");

		//directory watching
		this.directoryWatcher = directoryWatcher;
		this.directoryWatcherThread = new Thread(directoryWatcher);

		//message validation + processing
		this.messageProcessor=processor;
		this.messageProcessorThread = new Thread(messageProcessor);
		
		logger.trace("Placeholdermanager constructor return");
		
	}

	public void run() throws InterruptedException{
		
		logger.debug("PlaceHolderManager run");
		
		addShutdownHooks();
		
		logger.debug("starting directory watcher");
		directoryWatcherThread.start();
		logger.debug("starting message processor");
		messageProcessorThread.start();		
		
		logger.debug("Threads started");
		
		directoryWatcherThread.join();
		messageProcessorThread.join();
	}
	
	public void stop(){
		messageProcessor.stop();
		directoryWatcher.setContinueWatching(false);
	}

	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

		
			public void run() {
				stop();
			
			}
		}));
	}
}
