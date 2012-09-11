package com.mediasmiths.foxtel.placeholder.processing;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

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

	private final LinkedBlockingQueue<String> filePathsPending;
	private boolean stopRequested = false;

	private final Unmarshaller unmarhsaller;
	private final MayamClient mayamClient;
	private final MessageValidator messageValidator;

	public MessageProcessor(
			LinkedBlockingQueue<String> filePathsPendingProcessing,
			MessageValidator messageValidator, Unmarshaller unmarhsaller,
			MayamClient mayamClient) {
		this.filePathsPending = filePathsPendingProcessing;
		this.unmarhsaller = unmarhsaller;
		this.mayamClient = mayamClient;
		this.messageValidator = messageValidator;
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

			// TODO : indicate this was a result of service failure rather than
			// a problem with the request?
			throw new MessageProcessingFailedException();
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
		if (result == MayamClientErrorCode.SUCCESS) {
			logger.info("Action successfully processed");
		} else {
			logger.error(String.format(
					"Failed to process action, result was %s",
					result.toString()));
			throw new MessageProcessingFailedException();
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

	private void processFile(String filePath) {
		// we assume that the file at filePath has already been validated

		try {

			Object unmarshalled = unmarhsaller.unmarshal(new File(filePath));
			logger.debug(String.format("unmarshalled object of type %s",
					unmarshalled.getClass().toString()));

			PlaceholderMessage message = (PlaceholderMessage) unmarshalled;
			try {
				processPlaceholderMesage(message);
			} catch (MessageProcessingFailedException e) {
				logger.error(String.format("Message processing failed for %s",
						filePath), e);

				// TODO : handle failed messages
			}

		} catch (JAXBException e) {
			logger.fatal("A previously validated file did not unmarshall sucessfully, this is very bad");
		} catch (ClassCastException cce) {
			logger.fatal("A prevously validated file did not have an action of one of the expected types");
		}

	}

	private void processPlaceholderMesage(PlaceholderMessage message)
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
		}
	}

	private void purgeTitle(PurgeTitle action)
			throws MessageProcessingFailedException {
		MayamClientErrorCode result = mayamClient.purgeTitle(action);
		checkResult(result);
	}

	@Override
	//TODO this method is too big
	public void run() {

		while (!stopRequested) {
			try {
				String filePath = filePathsPending.take();

				try {
					MessageValidationResult result = messageValidator
							.validateFile(filePath);

					if (result == MessageValidationResult.IS_VALID) {
						logger.info(String
								.format("Placeholder message at %s validates",
										filePath));
						try {
							processFile(filePath);
						} catch (Exception e) {
							logger.error(String.format("Error processing %s",
									filePath), e);
						}
					} else {
						// TODO: move erroroneous xmls to some configurable
						// location

						logger.warn(String.format(
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

			} catch (InterruptedException e) {
				logger.warn("Interruped!", e);
			}
		}

	}

	public void stop() {
		stopRequested = true;
	}
}
