package com.mediasmiths.foxtel.placeholder.processing;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.placeholder.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.placeholder.receipt.ReceiptWriter;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

/**
 * Processes placeholder messages taken from a queue
 * 
 * @author Mediasmiths Forge
 * @see MessageValidator
 * 
 */
public class MessageProcessor implements Runnable {

	private static Logger logger = Logger.getLogger(MessageProcessor.class);

	protected final FilesPendingProcessingQueue filePathsPending;
	private boolean stopRequested = false;

	private final Unmarshaller unmarhsaller;
	private final MayamClient mayamClient;
	private final MessageValidator messageValidator;
	private final ReceiptWriter receiptWriter;
	private final String failurePath;
	private final String archivePath;

	@Inject
	public MessageProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			MessageValidator messageValidator, ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller, MayamClient mayamClient,
			@Named("placeholder.path.failure") String failurePath,
			@Named("placeholder.path.archive") String archivePath) {
		this.filePathsPending = filePathsPendingProcessing;
		this.unmarhsaller = unmarhsaller;
		this.mayamClient = mayamClient;
		this.messageValidator = messageValidator;
		this.receiptWriter = receiptWriter;
		this.failurePath = failurePath;
		this.archivePath = archivePath;
		logger.debug("Using failure path " + failurePath);
		logger.debug("Using archivePath path " + archivePath);
	}

	private void addOrUpdateMaterial(AddOrUpdateMaterial action)
			throws MessageProcessingFailedException {
		try {
			MayamClientErrorCode result;

			if (mayamClient.materialExists(action.getMaterial().getMaterialD())) {
				result = mayamClient.updateMaterial(action.getMaterial());
			} else {
				result = mayamClient.createMaterial(action.getMaterial());
			}

			checkResult(result);

		} catch (MayamClientException e) {
			logger.error(String.format(
					"MayamClientException querying if material %s exists",
					action.getMaterial().getMaterialD()), e);
			throw new MessageProcessingFailedException(
					MesageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION);
		}
	}

	private void addOrUpdatePackage(AddOrUpdatePackage action)
			throws MessageProcessingFailedException {

		try {

			MayamClientErrorCode result;

			if (mayamClient.packageExists(action.getPackage()
					.getPresentationID())) {
				result = mayamClient.updatePackage(action.getPackage());
			} else {
				result = mayamClient.createPackage(action.getPackage());
			}

			checkResult(result);

		} catch (MayamClientException e) {
			logger.error(String.format(
					"MayamClientException querying if package %s exists",
					action.getPackage().getPresentationID()), e);
			throw new MessageProcessingFailedException(
					MesageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION);
		}
	}

	/**
	 * Checks if a MayamClientErrorCode indicated success or not
	 * 
	 * @param result
	 * @throws MessageProcessingFailedException
	 *             if result != MayamClientErrorCode.SUCCESS
	 */
	private void checkResult(MayamClientErrorCode result)
			throws MessageProcessingFailedException {
		logger.trace("checkResult(" + result + ")");

		if (result == MayamClientErrorCode.SUCCESS) {
			logger.info("Action successfully processed");
		} else {
			logger.error(String.format(
					"Failed to process action, result was %s", result));
			throw new MessageProcessingFailedException(
					MesageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
		}
	}

	private void createOrUpdateTitle(CreateOrUpdateTitle action)
			throws MessageProcessingFailedException {

		try {
			MayamClientErrorCode result;

			if (mayamClient.titleExists(action.getTitleID())) {
				result = mayamClient.updateTitle(action);
			} else {
				result = mayamClient.createTitle(action);
			}

			checkResult(result);
		} catch (MayamClientException e) {
			logger.error(String.format(
					"MayamClientException querying if title %s exists",
					action.getTitleID()), e);
			throw new MessageProcessingFailedException(
					MesageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION);
		}
	}

	private void deleteMaterial(DeleteMaterial action)
			throws MessageProcessingFailedException {

		MayamClientErrorCode result = mayamClient.deleteMaterial(action);
		checkResult(result);

	}

	private void deletePackage(DeletePackage action)
			throws MessageProcessingFailedException {

		MayamClientErrorCode result = mayamClient.deletePackage(action);
		checkResult(result);

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

			Object unmarshalled = unmarhsaller.unmarshal(new File(filePath));
			logger.debug(String.format("unmarshalled object of type %s",
					unmarshalled.getClass().toString()));

			PlaceholderMessage message = (PlaceholderMessage) unmarshalled;
			try {
				processPlaceholderMesage(message);
			} catch (MessageProcessingFailedException e) {
				logger.error(String.format(
						"Message processing failed for %s and reason %s",
						filePath, e.getReason()), e);
				throw e;
			}

			return message.getMessageID();

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
	 * Processes a PlaceHoldermessage (which is assumed to have already been
	 * validated)
	 * 
	 * @param message
	 * @throws MessageProcessingFailedException
	 */
	public void processPlaceholderMesage(PlaceholderMessage message)
			throws MessageProcessingFailedException {
		Object action = message.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);

		boolean isCreateOrUpdateTitle = (action instanceof CreateOrUpdateTitle);
		boolean isPurgeTitle = (action instanceof PurgeTitle);
		boolean isAddOrUpdateMaterial = (action instanceof AddOrUpdateMaterial);
		boolean isDeleteMaterial = (action instanceof DeleteMaterial);
		boolean isAddOrUpdatePackage = (action instanceof AddOrUpdatePackage);
		boolean isDeletePackage = (action instanceof DeletePackage);

		if (isCreateOrUpdateTitle) {
			createOrUpdateTitle((CreateOrUpdateTitle) action);
		} else if (isPurgeTitle) {
			purgeTitle((PurgeTitle) action);
		} else if (isAddOrUpdateMaterial) {
			addOrUpdateMaterial((AddOrUpdateMaterial) action);
		} else if (isDeleteMaterial) {
			deleteMaterial((DeleteMaterial) action);
		} else if (isAddOrUpdatePackage) {
			addOrUpdatePackage((AddOrUpdatePackage) action);
		} else if (isDeletePackage) {
			deletePackage((DeletePackage) action);
		} else {
			logger.fatal("Either there is a new action type or something has gone very wrong");
		}
	}

	private void purgeTitle(PurgeTitle action)
			throws MessageProcessingFailedException {
		logger.trace("mayamClient.purgeTitle(...)");
		MayamClientErrorCode result = mayamClient.purgeTitle(action);
		checkResult(result);
	}

	protected void validateThenProcessFile(String filePath) {
		try {
			MessageValidationResult result = messageValidator
					.validateFile(filePath);

			if (result == MessageValidationResult.IS_VALID) {
				logger.info(String.format(
						"Placeholder message at %s validates", filePath));
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
						"Placeholder message at %s did not validate", filePath));
				moveMessageToFailureFolder(filePath);
			}

		} catch (MayamClientException e) {
			logger.error(String.format(
					"MayamClientException validating placeholder message %s",
					e.getErrorcode()), e);
			moveMessageToFailureFolder(filePath);
		}
	}

	/**
	 * Moves erroneous placeholder messages to a configurable location
	 * 
	 * @param messagePath
	 * @param messageID
	 */
	private void moveMessageToFailureFolder(String messagePath) {
		logger.info(String
				.format("Message %s is invalid, sending to failure folder",
						messagePath));
		logger.debug(String.format("Failure folder is: %s ", failurePath));

		try {
			moveMessageToFolder(messagePath, failurePath);
		} catch (IOException e) {
			logger.error(String.format(
					"IOException moving invalid placeholder message %s to %s",
					messagePath, failurePath), e);
		}

	}

	/**
	 * Moves placeholder messages which have been processes, to the arcive paths
	 * @param messagePath
	 */
	private void moveMessageToArchiveFolder(String messagePath) {
		logger.info(String.format(
				"Message %s is complete, sending to archive folder",
				messagePath));
		logger.debug(String.format("Archive folder is: %s ", archivePath));

		try {
			moveMessageToFolder(messagePath, archivePath);
		} catch (IOException e) {
			logger.error(String.format(
					"IOException moving placeholder message %s to archive %s",
					messagePath, archivePath), e);
		}

	}

	private void moveMessageToFolder(String messagePath,
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

	public void stop() {
		stopRequested = true;
	}
}
