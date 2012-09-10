package com.mediasmiths.foxtel.placeholder;

import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.placeholder.processing.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidatorRunnable;
import com.mediasmiths.mayam.MayamClient;

public class PlaceHolderManager {

	static Logger logger = Logger.getLogger(PlaceHolderManager.class);

	private final LinkedBlockingQueue<String> filePathsPendingValidation = new LinkedBlockingQueue<String>();
	private final LinkedBlockingQueue<String> filePathsPendingProcessing = new LinkedBlockingQueue<String>();

	private final MessageValidator messageValidator;
	private final MessageValidatorRunnable messageValidatorRunnable;
	private final Thread messageValidatorThread;

	private final MessageProcessor messageProcessor;
	private final Thread messageProcessorThread;

	private final PlaceHolderMessageDirectoryWatcher directoryWatcher;
	private final Thread directoryWatcherThread;

	public PlaceHolderManager(MayamClient mc,
			PlaceHolderManagerConfiguration config) throws JAXBException,
			SAXException {

		logger.trace("Placeholdermanager constructor enter");
		
		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		//directory watching
		directoryWatcher = new PlaceHolderMessageDirectoryWatcher(filePathsPendingValidation, config.getMessagePath());
		directoryWatcherThread = new Thread(directoryWatcher);
		
		//message validation
		messageValidator = new MessageValidator(unmarshaller, mc);
		messageValidatorRunnable = new MessageValidatorRunnable(filePathsPendingValidation, messageValidator, filePathsPendingProcessing);
		messageValidatorThread = new Thread(messageValidatorRunnable);
		
		//message processing
		messageProcessor = new MessageProcessor(filePathsPendingProcessing, unmarshaller,mc);
		messageProcessorThread = new Thread(messageProcessor);
		
		logger.trace("Placeholdermanager constructor return");
		
	}

	public void run(){
		
		logger.debug("PlaceHolderManager run");
		
		addShutdownHooks();
		
		logger.trace("starting directory watcher");
		directoryWatcherThread.start();
		logger.trace("starting message validator");
		messageValidatorThread.start();
		logger.trace("starting message processor");
		messageProcessorThread.start();		
		
		logger.debug("Threads started");
	}

	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

		
			public void run() {
				messageValidatorRunnable.stop();
				messageProcessor.stop();
				directoryWatcher.setContinueWatching(false);
			}
		}));
	}
}
