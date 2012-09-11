package com.mediasmiths.foxtel.placeholder.messagecreation;

import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.mediasmiths.foxtel.placeholder.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.placeholder.processing.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.receipt.ReceiptWriter;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;

/**
 * Processes only one message after starting, used for testing only
 * @author bmcleod
 *
 */
public class SingleMessageProcessor extends MessageProcessor {

	protected static Logger logger = Logger
			.getLogger(SingleMessageProcessor.class);

	public SingleMessageProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			MessageValidator messageValidator, ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller, MayamClient mayamClient) {
		super(filePathsPendingProcessing, messageValidator, receiptWriter,
				unmarhsaller, mayamClient);
	}

	@Override
	public void run() {

		try {
			String filePath = filePathsPending.take();
			validateThenProcessFile(filePath);
		} catch (InterruptedException e) {
			logger.info("Interruped!", e);
			stop();
		}

	}

}
