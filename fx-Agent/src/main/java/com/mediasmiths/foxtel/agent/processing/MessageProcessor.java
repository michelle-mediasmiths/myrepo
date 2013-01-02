
package com.mediasmiths.foxtel.agent.processing;


import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;

/**
 * Processes messages taken from a queue
 * 
 * @author Mediasmiths Forge
 * @see MessageValidator
 * 
 */
public abstract class MessageProcessor<T> implements Runnable {

	private static Logger logger = Logger.getLogger(MessageProcessor.class);

	private final FilesPendingProcessingQueue filePathsPending;
	
	protected final Unmarshaller unmarhsaller;	
	protected final Marshaller marshaller;

	
	public final static String FAILUREFOLDERNAME="failed";
	public final static String ARCHIVEFOLDERNAME="completed"; 	
	
	private final MessageValidator<T> messageValidator;
	private final ReceiptWriter receiptWriter;
	protected final EventService eventService;

    //private final EventPickUpTimings pickUpEventTimer;
	
	@Inject
	public MessageProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			MessageValidator<T> messageValidator, ReceiptWriter receiptWriter,
			Unmarshaller unmarshaller, Marshaller marshaller, EventService eventService) {
		this.filePathsPending = filePathsPendingProcessing;
		this.unmarhsaller = unmarshaller;
		this.marshaller = marshaller;
		this.messageValidator = messageValidator;
		this.receiptWriter = receiptWriter;
		this.eventService = eventService;
       // this.pickUpEventTimer = pickupEventTimer;
	}

	/**
	 * Processes a given filepath : assumes it has already been validated
	 * 
	 * @param filePath
	 * @throws MessageProcessingFailedException
	 * @returns the messageID of the message at filePath
	 */
	public String processFile(String filePath)
			throws MessageProcessingFailedException {

		try {
            File fileLoc = new File(filePath);
			Object unmarshalled = unmarhsaller.unmarshal(fileLoc);

            //pickUpEventTimer.saveEvent(getPickUpTimingDetails(fileLoc));

            if (logger.isDebugEnabled())
                logger.debug(String.format("unmarshalled object of type %s", unmarshalled.getClass().toString()));

			typeCheck(unmarshalled);

			@SuppressWarnings("unchecked")
			T message = (T) unmarshalled;
			MessageEnvelope<T> envelope = new MessageEnvelope<T>(fileLoc, message);
			try {
				processMessage(envelope);
			} catch (MessageProcessingFailedException e) {
				logger.error(String.format(
						"Message processing failed for %s and reason %s",
						filePath, e.getReason()), e);
				
				//TODO send failure reason along with xml
				eventService.saveEvent("error", message);
				
				throw e;
			}

			return getIDFromMessage(envelope);

		} catch (JAXBException e) {
			logger.error("A previously validated file did not unmarshall sucessfully, this is very bad");
			throw new MessageProcessingFailedException(
					MessageProcessingFailureReason.UNMARSHALL_FAILED, e);
		} catch (ClassCastException cce) {
			logger.error(
					"A prevously validated file did not have an action of one of the expected types or a mayam attribute was not of the expected type",
					cce);
			throw new MessageProcessingFailedException(
					MessageProcessingFailureReason.UNKNOWN_ACTION, cce);
		}
		

	}

    /**
     *
     * @param fileLoc the file that has just been picked up for processing.
     * @return  the details about pick up time suitable for the eventing system

    private FilePickUpTimings getPickUpTimingDetails(File fileLoc)
    {
        FilePickUpTimings pickUpStats = new FilePickUpTimings();
        pickUpStats.set();
        return pickUpStats;
    }

    */

    /**
	 * Called to check the type of an unmarshalled object, left up to
	 * implementing classes as (unmarshalled instanceof T) cannot be checked;
	 * 
	 * @param unmsarhalled
	 * @throws ClassCastException
	 *             if the unmarshalled object is not of the expected type
	 */
	protected abstract void typeCheck(Object unmarshalled)
			throws ClassCastException;

	protected abstract String getIDFromMessage(MessageEnvelope<T> envelope);

	protected final void validateThenProcessFile(String filePath) {

		if (!new File(filePath).exists())
		{
			logger.warn("file path passed for validation does not exist, usually this means it has already been processed :"
					+ filePath);
		}
		else
		{

			logger.debug("Asking for validation of " + filePath);

			MessageValidationResult result;
			try
			{
				result = messageValidator.validateFile(filePath);
			}
			catch (Exception e)
			{
				logger.error("uncaught exception validating file", e);
				result = MessageValidationResult.UNKOWN_VALIDATION_FAILURE;
			}

			if (result == MessageValidationResult.IS_VALID)
			{
				logger.info(String.format("Message at %s validates", filePath));
				try
				{
					String messageID = processFile(filePath);
					writeReceipt(filePath, messageID);
					if (shouldArchiveMessages())
					{
						moveMessageToArchiveFolder(filePath);
					}
				}
				catch (MessageProcessingFailedException e)
				{
					logger.error(String.format("Error processing %s", filePath), e);
					moveFileToFailureFolder(new File(filePath));
				}
				catch (Exception e)
				{
					logger.error(String.format("uncaught exception processing file %s", filePath), e);
					moveFileToFailureFolder(new File(filePath));
				}

			}
			else
			{
				logger.warn(String.format("Message at %s did not validate", filePath));
				messageValidationFailed(filePath, result);
				moveFileToFailureFolder(new File(filePath));
			}
		}

	}

	/**
	 * informs MessageProcesors of a message validation failure, does not move
	 * messages to the failure folder
	 * 
	 * @param filePath
	 * @param result
	 */
	protected abstract void messageValidationFailed(String filePath,
			MessageValidationResult result);

	protected abstract void processMessage(MessageEnvelope<T> envelope)
			throws MessageProcessingFailedException;

	/**
	 * Moves erroneous messages to a configurable location
	 * 
	 * @param messagePath
	 * @param messageID
	 */
	protected void moveFileToFailureFolder(File file) {
		
		logger.info(String.format(
				"File %s is invalid, sending to failure folder",
				file.getAbsolutePath()));
		String failurePath = getFailureFolderForFile(file);
		
		logger.debug(String.format("Failure folder is: %s ", failurePath));

		try {
			moveFileToFolder(file, failurePath,true);
		} catch (IOException e) {
			logger.error(String.format(
					"IOException moving invalid file %s to %s",
					file.getAbsolutePath(), failurePath), e);
		}
	}

	
	public static String getFailureFolderForFile(File file) {
		
		String pathToFile = file.getAbsolutePath();		
		String failurePath = FilenameUtils.getFullPath(pathToFile) + FAILUREFOLDERNAME + IOUtils.DIR_SEPARATOR;		
		logger.debug(String.format("returning failure folder %s for file %s ", failurePath,pathToFile));
		
		return failurePath;
	}

	/**
	 * Moves messages which have been processed, to the archive path
	 * 
	 * @param messagePath
	 */
	private void moveMessageToArchiveFolder(String messagePath) {
		logger.info(String.format(
				"Message %s is complete, sending to archive folder",
				messagePath));
		String archivePath = getArchivePathForFile(messagePath);
		logger.debug(String.format("Archive folder is: %s ", archivePath));

		try {
			moveFileToFolder(new File(messagePath), archivePath, true);
		} catch (IOException e) {

			logger.warn(String.format(
					"IOException moving message %s to archive %s", messagePath,
					archivePath), e);
		}

	}

	public static String getArchivePathForFile(String messagePath) {
		
		String pathToFile = messagePath;		
		String archivePath = FilenameUtils.getFullPath(pathToFile) + ARCHIVEFOLDERNAME + IOUtils.DIR_SEPARATOR;		
		logger.debug(String.format("returning archivePath folder %s for file %s ", archivePath,pathToFile));
		
		return archivePath;
	}

	protected synchronized void moveFileToFolder(File file,
			String destinationFolderPath, boolean ensureUniqueFirst)
			throws IOException {
		final String destination = getDestinationPathForFileMove(file,
				destinationFolderPath, ensureUniqueFirst);

		logger.trace(String.format("Moving file from %s to %s",
				file.getAbsolutePath(), destination));

		FileUtils.moveFile(file, new File(destination));
	}

	public static String getDestinationPathForFileMove(File file,
			String destinationFolderPath, boolean ensureUnique) {
		
		StringBuilder sb = new StringBuilder(destinationFolderPath);
		
		//only add slash if required
		if (destinationFolderPath.charAt(destinationFolderPath.length() - 1) != IOUtils.DIR_SEPARATOR) {
			sb.append(IOUtils.DIR_SEPARATOR);
		}
		sb.append(FilenameUtils.getName(file.getAbsolutePath()));
		
		String destination = sb.toString();
		
		if(ensureUnique){
			File dest = new File(destination);
			
			int i=1;
			
			while(dest.exists()){
				logger.warn(String.format("Destination file %s already exists",	destination));
				
				sb = new StringBuilder(destinationFolderPath);
				sb.append(IOUtils.DIR_SEPARATOR);
				sb.append(FilenameUtils.getBaseName(file.getAbsolutePath()));
				sb.append("_");
				sb.append(i);
				sb.append(FilenameUtils.EXTENSION_SEPARATOR);
				sb.append(FilenameUtils.getExtension(file.getAbsolutePath()));
				destination = sb.toString();
				dest = new File(destination);		
				i++;
			}
		}
		logger.trace("returning destination"+destination);
		return destination;
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

		while (!Thread.interrupted()) {
			try {
				String filePath = getFilePathsPending().take();

				if (isMessage(filePath)) {
					validateThenProcessFile(filePath);
				} else {
					processNonMessageFile(filePath);
				}

			} catch (InterruptedException e) {
				logger.info("Interruped!", e);
				return;
			} catch (Exception e) {
				logger.fatal(
						"Uncaught exception almost killed MessageProcessor thread, this is very bad",
						e);
				
				eventService.saveEvent("error", "Uncaught exception almost killed MessageProcessor");
			}
		}

	}

	protected abstract void processNonMessageFile(String filePath);

	protected abstract boolean shouldArchiveMessages();

	protected boolean isMessage(String filePath) {
		return FilenameUtils.getExtension(filePath).toLowerCase(Locale.ENGLISH)
				.equals("xml");
	}

	public FilesPendingProcessingQueue getFilePathsPending() {
		return filePathsPending;
	}
}
