package com.mediasmiths.foxtel.placeholder.validmessagepickup;

import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;

import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.Config;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.placeholder.processing.PlaceholderMessageProcessor;
import com.mediasmiths.foxtel.placeholder.validation.PlaceholderMessageValidator;
import com.mediasmiths.mayam.MayamClient;


/**
 * Processes only one message after starting, used for testing only
 * @author bmcleod
 *
 */
public class SingleMessageProcessor extends PlaceholderMessageProcessor {

	protected static Logger logger = Logger
			.getLogger(SingleMessageProcessor.class);

	@Inject
	public SingleMessageProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			PlaceholderMessageValidator messageValidator, ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller, MayamClient mayamClient,  @Named(FAILURE_PATH)  String failurePath,@Named(Config.ARCHIVE_PATH)  String archivePath) {
		super(filePathsPendingProcessing, messageValidator, receiptWriter,
				unmarhsaller, mayamClient,failurePath,archivePath);
	}
	
	@Override
	public void run() {

		logger.trace("SingleMessageProcessor.run() enter");

		try {
			String filePath = getFilePathsPending().take();
			validateThenProcessFile(filePath);
		} catch (InterruptedException e) {
			logger.info("Interruped!", e);
			stop();
		}

		logger.trace("SingleMessageProcessor.run() exit");

	}


}
