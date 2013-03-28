package com.mediasmiths.foxtel.placeholder.validmessagepickup;

import java.io.File;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.FilePickUpProcessingQueue;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.placeholder.processing.PlaceholderMessageProcessor;
import com.mediasmiths.foxtel.placeholder.validation.PlaceholderMessageValidator;
import com.mediasmiths.mayam.MayamClient;


/**
 * Processes only one message after starting, used for testing only
 * @author bmcleod
 *
 */
public class SingleMessageProcessor extends PlaceholderMessageProcessor {

	@Inject
	public SingleMessageProcessor(
			FilePickUpProcessingQueue filePathsPendingProcessing,
			PlaceholderMessageValidator messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			Marshaller marshaller,
			MayamClient mayamClient,
			EventService eventService) {
		super(filePathsPendingProcessing, messageValidator, receiptWriter,
				unmarhsaller,marshaller, mayamClient, eventService);
	}


	protected static Logger logger = Logger
			.getLogger(SingleMessageProcessor.class);

	
	@Override
	public void run() {

		logger.trace("SingleMessageProcessor.run() enter");

		File file = getFilePathsPending().take();
		String filePath = file.getAbsolutePath();
		validateThenProcessPickupPackage(filePath);

		logger.trace("SingleMessageProcessor.run() exit");

	}


}
