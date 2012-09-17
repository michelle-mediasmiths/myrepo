package com.mediasmiths.foxtel.placeholder.processing;

import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailureReason;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.validation.PlaceholderMessageValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

/**
 * Processes placeholder messages taken from a queue
 * 
 * @author Mediasmiths Forge
 * @see PlaceholderMessageValidator
 * 
 */
public class PlaceholderMessageProcessor extends MessageProcessor<PlaceholderMessage> {

	private static Logger logger = Logger.getLogger(PlaceholderMessageProcessor.class);

	private final MayamClient mayamClient;

	@Inject
	public PlaceholderMessageProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			PlaceholderMessageValidator messageValidator, ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller, MayamClient mayamClient,
			@Named("agent.path.failure") String failurePath,
			@Named("agent.path.archive") String archivePath) {
		super(filePathsPendingProcessing,messageValidator,receiptWriter,unmarhsaller,failurePath,archivePath);
		this.mayamClient = mayamClient;
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
					MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION,e);
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
					MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION,e);
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
					MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
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
					MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION,e);
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
	 * Processes a PlaceHoldermessage (which is assumed to have already been
	 * validated)
	 * 
	 * @param message
	 * @throws MessageProcessingFailedException
	 */
	@Override
	public void processMessage(MessageEnvelope<PlaceholderMessage> envelope)
			throws MessageProcessingFailedException {
		
		PlaceholderMessage message = envelope.getMessage();
		
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

	@Override
	protected String getIDFromMessage(MessageEnvelope<PlaceholderMessage> envelope) {
		return envelope.getMessage().getMessageID();
	}

	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException {
		
		if(! (unmarshalled instanceof PlaceholderMessage)){
			throw new ClassCastException(String.format("unmarshalled type %s is not a PlaceholderMessage",
					unmarshalled.getClass().toString()));
		}
		
	}

	@Override
	protected void processNonMessageFile(String filePath) {
		logger.error("Placeholder Agent does not expect non message files");	
		throw new RuntimeException(String.format("Placeholder Agent does not expect non message files %s",filePath));
	}

	@Override
	protected boolean shouldArchiveMessages() {
		return true;
	}

	@Override
	protected void messageValidationFailed(String filePath,
			MessageValidationResult result) {
	
		//TODO notify someone of the failure
		
	}

}
