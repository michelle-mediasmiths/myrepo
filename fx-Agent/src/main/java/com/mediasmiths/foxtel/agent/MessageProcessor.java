package com.mediasmiths.foxtel.agent;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Processes messages taken from a queue
 * 
 * @author Mediasmiths Forge
 * @see MessageValidator
 * 
 */
public abstract class MessageProcessor<T> implements Runnable {

	private static Logger logger = Logger.getLogger(MessageProcessor.class);

	protected final FilesPendingProcessingQueue filePathsPending;
	private boolean stopRequested = false;

	private final Unmarshaller unmarhsaller;
	private final MessageValidator<T> messageValidator;
	private final ReceiptWriter receiptWriter;
	private final String failurePath;
	private final String archivePath;

	@Inject
	public MessageProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			MessageValidator<T> messageValidator, ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			@Named("agent.path.failure") String failurePath,
			@Named("agent.path.archive") String archivePath) {
		this.filePathsPending = filePathsPendingProcessing;
		this.unmarhsaller = unmarhsaller;
		this.messageValidator = messageValidator;
		this.receiptWriter = receiptWriter;
		this.failurePath = failurePath;
		this.archivePath = archivePath;
		logger.debug("Using failure path " + failurePath);
		logger.debug("Using archivePath path " + archivePath);
	}

	/**
	 * Processes a given filepath : assumes it has already been validated
	 * 
	 * @param filePath
	 * @throws MessageProcessingFailedException
	 * @returns the messageID of the message at filePath
	 */
	public final String processFile(String filePath)
			throws MessageProcessingFailedException {

		try {

			Object unmarshalled = unmarhsaller.unmarshal(new File(filePath));
			logger.debug(String.format("unmarshalled object of type %s",
					unmarshalled.getClass().toString()));

			typeCheck(unmarshalled);
			
			@SuppressWarnings("unchecked")
			T message = (T) unmarshalled;
			try {
				processMessage(message);
			} catch (MessageProcessingFailedException e) {
				logger.error(String.format(
						"Message processing failed for %s and reason %s",
						filePath, e.getReason()), e);
				throw e;
			}

			return getIDFromMessage(message);

		} catch (JAXBException e) {
			logger.fatal("A previously validated file did not unmarshall sucessfully, this is very bad");
			throw new MessageProcessingFailedException(
					MesageProcessingFailureReason.UNMARSHALL_FAILED);
		} catch (ClassCastException cce) {
			logger.fatal("A prevously validated file did not have an action of one of the expected types");
			throw new MessageProcessingFailedException(
					MesageProcessingFailureReason.UNKNOWN_ACTION);
		}

	}
	
	/**
	 *  Called to check the type of an unmarshalled object, left up to implementing classes as (unmarshalled instanceof T) cannot be checked;
	 * @param unmsarhalled
	 * @throws ClassCastException if the unmarshalled object is not of the expected type
	 */
	protected abstract void typeCheck(Object unmarshalled) throws ClassCastException;

	
	protected abstract String getIDFromMessage(T message);

	protected final void validateThenProcessFile(String filePath) {
		try {
			MessageValidationResult result = messageValidator
					.validateFile(filePath);

			if (result == MessageValidationResult.IS_VALID) {
				logger.info(String.format(
						"Message at %s validates", filePath));
				try {
					String messageID = processFile(filePath);
					writeReceipt(filePath, messageID);
					moveMessageToArchiveFolder(filePath);
				} catch (MessageProcessingFailedException e) {
					logger.error(
							String.format("Error processing %s", filePath), e);
					moveMessageToFailureFolder(filePath);
				}
			} else {
				logger.warn(String.format(
						"Message at %s did not validate", filePath));
				moveMessageToFailureFolder(filePath);
			}

		} catch (Exception e) {
			logger.error(String.format(
					"Exception validating message"), e);
			moveMessageToFailureFolder(filePath);
		}
	}
	
	protected abstract void processMessage(T message) throws MessageProcessingFailedException;
	
	/**
	 * Moves erroneous messages to a configurable location
	 * 
	 * @param messagePath
	 * @param messageID
	 */
	private final void moveMessageToFailureFolder(String messagePath) {
		logger.info(String
				.format("Message %s is invalid, sending to failure folder",
						messagePath));
		logger.debug(String.format("Failure folder is: %s ", failurePath));

		try {
			moveMessageToFolder(messagePath, failurePath);
		} catch (IOException e) {
			logger.error(String.format(
					"IOException moving invalid message %s to %s",
					messagePath, failurePath), e);
		}

	}

	/**
	 * Moves messages which have been processed, to the archive path
	 * @param messagePath
	 */
	private final void moveMessageToArchiveFolder(String messagePath) {
		logger.info(String.format(
				"Message %s is complete, sending to archive folder",
				messagePath));
		logger.debug(String.format("Archive folder is: %s ", archivePath));

		try {
			moveMessageToFolder(messagePath, archivePath);
		} catch (IOException e) {
			logger.error(String.format(
					"IOException moving message %s to archive %s",
					messagePath, archivePath), e);
		}

	}

	private final void moveMessageToFolder(String messagePath,
			String destinationFolderPath) throws IOException {
		final String destination = destinationFolderPath
				+ IOUtils.DIR_SEPARATOR + FilenameUtils.getName(messagePath);
		
		logger.trace(String.format("Moving file from %s to %s", messagePath,destination));
		
		FileUtils.moveFile(new File(messagePath), new File(destination));
	}

	private void writeReceipt(String filePath, String messageID) {
		try {
			receiptWriter.writeRecipet(filePath, messageID);
		} catch (IOException e) {
			logger.error("Failed to write receipt for message" + messageID);
		}
	}

	@Override
	public void run() {

		while (!stopRequested) {
			try {
				String filePath = filePathsPending.take();
				validateThenProcessFile(filePath);
			} catch (InterruptedException e) {
				logger.info("Interruped!", e);
				stop();
			}
		}

	}

	public final void stop() {
		stopRequested = true;
	}
}
