package com.mediasmiths.foxtel.placeholder.processing;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.placeholder.PlaceholderAgentConfiguration.PLACEHOLDER_MANAGEMENT_FAILURE_RECEIPIENT;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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
import com.mediasmiths.mayam.AlertInterface;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.stdEvents.persistence.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.rest.api.EventAPI;

/**
 * Processes placeholder messages taken from a queue
 * 
 * @author Mediasmiths Forge
 * @see PlaceholderMessageValidator
 * 
 */
public class PlaceholderMessageProcessor extends MessageProcessor<PlaceholderMessage>
{

	private static Logger logger = Logger.getLogger(PlaceholderMessageProcessor.class);

	private final MayamClient mayamClient;

	private final AlertInterface alert;
	private final String failureAlertRecipient;

	@Inject
	private EventAPI events;

	private Marshaller marshaller;

	@Inject
	public PlaceholderMessageProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			PlaceholderMessageValidator messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			Marshaller marshaller,
			MayamClient mayamClient,
			@Named(FAILURE_PATH) String failurePath,
			@Named(ARCHIVE_PATH) String archivePath,
			AlertInterface alert,
			@Named(PLACEHOLDER_MANAGEMENT_FAILURE_RECEIPIENT) String failureAlertRecipient)
	{
		super(filePathsPendingProcessing, messageValidator, receiptWriter, unmarhsaller, failurePath, archivePath);
		this.mayamClient = mayamClient;
		this.alert = alert;
		this.failureAlertRecipient = failureAlertRecipient;
		this.marshaller = marshaller;
		logger.debug("Using failure path: " + failurePath);
		logger.debug("Using archivePath path: " + archivePath);
	}

	private void addOrUpdateMaterial(AddOrUpdateMaterial action) throws MessageProcessingFailedException
	{
		try
		{
			MayamClientErrorCode result;

			if (mayamClient.materialExists(action.getMaterial().getMaterialID()))
			{
				result = mayamClient.updateMaterial(action.getMaterial());
			}
			else
			{
				result = mayamClient.createMaterial(action.getMaterial());
			}

			checkResult(result);

		}
		catch (MayamClientException e)
		{
			logger.error(
					String.format("MayamClientException querying if material %s exists", action.getMaterial().getMaterialID()),
					e);
			throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION, e);
		}
	}

	private void addOrUpdatePackage(AddOrUpdatePackage action) throws MessageProcessingFailedException
	{

		try
		{

			MayamClientErrorCode result;

			if (mayamClient.packageExists(action.getPackage().getPresentationID()))
			{
				result = mayamClient.updatePackage(action.getPackage());
			}
			else
			{
				result = mayamClient.createPackage(action.getPackage());
			}

			checkResult(result);

		}
		catch (MayamClientException e)
		{
			logger.error(
					String.format("MayamClientException querying if package %s exists", action.getPackage().getPresentationID()),
					e);
			throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION, e);
		}
	}

	/**
	 * Checks if a MayamClientErrorCode indicated success or not
	 * 
	 * @param result
	 * @throws MessageProcessingFailedException
	 *             if result != MayamClientErrorCode.SUCCESS
	 */
	private void checkResult(MayamClientErrorCode result) throws MessageProcessingFailedException
	{
		logger.trace("checkResult(" + result + ")");

		if (result == MayamClientErrorCode.SUCCESS)
		{
			logger.info("Action successfully processed");
			System.out.println("\n");
		}
		else
		{
			logger.error(String.format("Failed to process action, result was %s", result));
			throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
		}
	}

	private void createOrUpdateTitle(CreateOrUpdateTitle action) throws MessageProcessingFailedException
	{

		try
		{
			MayamClientErrorCode result;

			if (mayamClient.titleExists(action.getTitleID()))
			{
				result = mayamClient.updateTitle(action);
			}
			else
			{
				result = mayamClient.createTitle(action);
			}

			checkResult(result);
		}
		catch (MayamClientException e)
		{
			logger.error(String.format("MayamClientException querying if title %s exists", action.getTitleID()), e);
			throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION, e);
		}
	}

	private void deleteMaterial(DeleteMaterial action) throws MessageProcessingFailedException
	{

		MayamClientErrorCode result = mayamClient.deleteMaterial(action);
		checkResult(result);

	}

	private void deletePackage(DeletePackage action) throws MessageProcessingFailedException
	{

		MayamClientErrorCode result = mayamClient.deletePackage(action);
		checkResult(result);

	}

	/**
	 * Processes a PlaceHoldermessage (which is assumed to have already been validated)
	 * 
	 * @param message
	 * @throws MessageProcessingFailedException
	 */
	@Override
	public void processMessage(MessageEnvelope<PlaceholderMessage> envelope) throws MessageProcessingFailedException
	{

		PlaceholderMessage message = envelope.getMessage();

		Object action = message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);

		boolean isCreateOrUpdateTitle = (action instanceof CreateOrUpdateTitle);
		boolean isPurgeTitle = (action instanceof PurgeTitle);
		boolean isAddOrUpdateMaterial = (action instanceof AddOrUpdateMaterial);
		boolean isDeleteMaterial = (action instanceof DeleteMaterial);
		boolean isAddOrUpdatePackage = (action instanceof AddOrUpdatePackage);
		boolean isDeletePackage = (action instanceof DeletePackage);

		if (isCreateOrUpdateTitle)
		{
			createOrUpdateTitle((CreateOrUpdateTitle) action);
			saveEvent("CreateOrUpdateTitle", message);
		}
		else if (isPurgeTitle)
		{
			purgeTitle((PurgeTitle) action);
			saveEvent("PurgeTitle", message);
		}
		else if (isAddOrUpdateMaterial)
		{
			addOrUpdateMaterial((AddOrUpdateMaterial) action);
			saveEvent("AddOrUpdateMaterial", message);
		}
		else if (isDeleteMaterial)
		{
			deleteMaterial((DeleteMaterial) action);
			saveEvent("DeleteMaterial", message);
		}
		else if (isAddOrUpdatePackage)
		{
			addOrUpdatePackage((AddOrUpdatePackage) action);
			saveEvent("DeleteMaterial", message);
		}
		else if (isDeletePackage)
		{
			deletePackage((DeletePackage) action);
			saveEvent("DeletePackage", message);
		}
		else
		{
			logger.fatal("Either there is a new action type or something has gone very wrong");
		}
	}

	private void purgeTitle(PurgeTitle action) throws MessageProcessingFailedException
	{
		logger.trace("mayamClient.purgeTitle(...)");
		MayamClientErrorCode result = mayamClient.purgeTitle(action);
		checkResult(result);
	}

	@Override
	protected String getIDFromMessage(MessageEnvelope<PlaceholderMessage> envelope)
	{
		return envelope.getMessage().getMessageID();
	}

	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException
	{ // NOSONAR
		// throwing unchecked exception as hint to users of class that this
		// method is likely to throw ClassCastException

		if (!(unmarshalled instanceof PlaceholderMessage))
		{
			throw new ClassCastException(String.format(
					"unmarshalled type %s is not a PlaceholderMessage",
					unmarshalled.getClass().toString()));
		}

	}

	@Override
	protected void processNonMessageFile(String filePath)
	{
		logger.error("Placeholder Agent does not expect non message files");
		throw new RuntimeException(String.format("Placeholder Agent does not expect non message files %s", filePath));
	}

	@Override
	protected boolean shouldArchiveMessages()
	{
		return true;
	}

	@Override
	protected void moveFileToFailureFolder(File file){
		
		String message;
		try{
			message = FileUtils.readFileToString(file);
		}
		catch(IOException e){
			logger.warn("IOException reading "+file.getAbsolutePath(),e);
			message = file.getAbsolutePath();
		}
		saveEvent("error",message);
		
		super.moveFileToFailureFolder(file);
	}
	
	@Override
	protected void messageValidationFailed(String filePath, MessageValidationResult result)
	{

		String message;
		try{
			message = FileUtils.readFileToString(new File(filePath));
		}
		catch(IOException e){
			logger.warn("IOException reading "+filePath,e);
			message = filePath;
		}
		saveEvent("failed",message);
		
		// send out alert that there has been an error validating message
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(
				"Validation of Placeholder management message %s failed for reason %s",
				FilenameUtils.getName(filePath),
				result.toString()));
		alert.sendAlert(failureAlertRecipient, "Placeholder Message Validation Failure", sb.toString());

	}

	protected void saveEvent(String name, String payload)
	{
		try
		{
			EventEntity event = new EventEntity();
			event.setEventName(name);
			event.setNamespace("http://www.foxtel.com.au/ip/bms");

			event.setPayload(payload);
			event.setTime(System.currentTimeMillis());
			events.saveReport(event);
		}
		catch (RuntimeException re)
		{
			logger.error("error saving event" + name, re);
		}

	}

	protected void saveEvent(String name, Object payload)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.marshal(payload, baos);
			String sPayload = baos.toString("UTF-8");
			saveEvent(name, sPayload);
		}
		catch (JAXBException e)
		{
			logger.error("error saving event" + name, e);
		}
		catch (UnsupportedEncodingException e)
		{
			logger.error("error saving event" + name, e);
		}

	}
}
