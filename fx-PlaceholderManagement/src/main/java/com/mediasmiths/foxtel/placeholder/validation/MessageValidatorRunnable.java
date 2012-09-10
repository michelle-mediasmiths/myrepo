package com.mediasmiths.foxtel.placeholder.validation;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.mediasmiths.mayam.MayamClientException;

/**
 * Polls a pending validation queue, when a file arrives in the queue is is tested for validity
 * 
 * Valid files added to pending processing queue
 * 
 * @author Mediasmiths Forge
 *
 */
public class MessageValidatorRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(MessageValidatorRunnable.class);
	
	private boolean stopRequested = false;
	private MessageValidator messageValidator;
	private LinkedBlockingQueue<String> filePathsPendingValidation;
	private LinkedBlockingQueue<String> filePathsPendingProcessing;

	public MessageValidatorRunnable(
			LinkedBlockingQueue<String> filePathsPendingValidation,
			MessageValidator messageValidator,
			LinkedBlockingQueue<String> filePathsPendingProcessing) {
		this.filePathsPendingValidation = filePathsPendingValidation;
		this.filePathsPendingProcessing = filePathsPendingProcessing;
		this.messageValidator = messageValidator;
	}

	@Override
	public void run() {

		while (!stopRequested) {
			try {
				String filePath = filePathsPendingValidation.take();
				handleFilePath(filePath);

			} catch (InterruptedException e) {
				logger.warn("Interruped!", e);
			}
		}
	}

	private void handleFilePath(String filePath) {

		try {
			MessageValidationResult result = messageValidator
					.validateFile(filePath);

			if (result == MessageValidationResult.IS_VALID) {
				logger.info(String.format(
						"Placeholder message at %s validates", filePath));
				filePathsPendingProcessing.add(filePath);
			} else {
				//TODO: move erroroneous xmls to some configurable location
				
				logger.error(String.format(
						"Placeholder message at %s did not validate",
						filePath));
			}

		} catch (SAXException e) {
			logger.error("SAXException:", e);
		} catch (ParserConfigurationException e) {
			logger.error("ParserConfigurationException:", e);
		} catch (IOException e) {
			logger.error("IOException:", e);
		} catch (MayamClientException e) {
			logger.error(
					String.format("MayamClientException %s",
							e.getErrorcode()), e);
		}
	}

	public void stop() {
		stopRequested = true;
	}

}