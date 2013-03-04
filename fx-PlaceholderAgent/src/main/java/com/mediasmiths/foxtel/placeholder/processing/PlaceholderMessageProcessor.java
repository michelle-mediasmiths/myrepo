package com.mediasmiths.foxtel.placeholder.processing;

import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.ChannelType;
import au.com.foxtel.cf.mam.pms.Channels;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.License;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.Source;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailureReason;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.FilePickUpProcessingQueue;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
import com.mediasmiths.foxtel.ip.common.events.report.PurgeContent;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.placeholder.validation.PlaceholderMessageValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	
	@Inject
	@Named("fxreportserialiser")
	private JAXBSerialiser reportSerialiser;
	
	@Inject
	public PlaceholderMessageProcessor(
			FilePickUpProcessingQueue filePathsPendingProcessing,
			PlaceholderMessageValidator messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			Marshaller marshaller,
			MayamClient mayamClient,
			EventService eventService)
	{
		super(filePathsPendingProcessing, messageValidator, receiptWriter, unmarhsaller, marshaller,eventService);
		this.mayamClient = mayamClient;
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
				result = mayamClient.createMaterial(action.getMaterial(), action.getTitleID());

				// sendMaterialOrderStatus(action);

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

				// sendPackageOrderStatus(action);

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
			logger.info("\n");
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
				

				// sendTitleOrderStatus(action);

			}

			checkResult(result);
		}
		catch (MayamClientException e)
		{
			logger.error(String.format("MayamClientException querying if title %s exists", action.getTitleID()), e);
			throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION, e);
		}
	}

	private void sendMaterialOrderStatus(AddOrUpdateMaterial action){
		OrderStatus os = new OrderStatus();
		
		Source source = action.getMaterial().getSource();
		if(source.getAggregation() != null && source.getAggregation().getAggregator() != null){
			os.setAggregatorID(source.getAggregation().getAggregator().getAggregatorID());
		}
		if(source.getAggregation().getOrder() != null){
			os.setOrderRef(source.getAggregation().getOrder().getOrderReference());
		}
		os.setTitle("");
		os.setRequiredBy(action.getMaterial().getRequiredBy());
		os.setMaterialID(action.getMaterial().getMaterialID());
		os.setTaskType(MayamTaskListType.INGEST.getText());
		
		String osString = reportSerialiser.serialise(os);
		
		//send event
		eventService.saveEvent("OrderStatusReport", osString);
	}
	
	private void sendTitleOrderStatus(CreateOrUpdateTitle action)
	{
	
		OrderStatus os = new OrderStatus();

		List<String> channelsList = getChannels(action.getRights());
		
		String channelsString = StringUtils.join(channelsList, ',');
		os.setChannels(channelsString);
		os.setMaterialID(action.getTitleID());
		os.setTitle(action.getTitleDescription().getProgrammeTitle());
		
		String osString = reportSerialiser.serialise(os);
		
		//send event
		eventService.saveEvent("OrderStatusReport", osString);
		
	}

	private List<String> getChannels(final RightsType rights)
	{
		List<String> channelsList = new ArrayList<String>();

		if(rights!=null){
			List<License> licenses = rights.getLicense();
			if(licenses!=null){
				for (License l  : licenses)
				{
					Channels channels = l.getChannels();
					if(channels!= null){
						List<ChannelType> channel = channels.getChannel();
						if(channel!=null){
							for (ChannelType channelType : channel)
							{
								channelsList.add(channelType.getChannelTag());
							}
						}
					}
				}
			}
		}
		return channelsList;
	}

	private void sendPackageOrderStatus(AddOrUpdatePackage action){
		
		OrderStatus os = new OrderStatus();
		os.setMaterialID(action.getPackage().getPresentationID());
		String osString = reportSerialiser.serialise(os);
		
		//send event
		eventService.saveEvent("OrderStatusReport", osString);
	}

	private void sendPurgePackage(MayamClientErrorCode opCode, DeletePackage action)
	{

		if (opCode == MayamClientErrorCode.SUCCESS)
		{
			PurgeContent pc = new PurgeContent();
			pc.setMaterialID(action.getPackage().getPresentationID());

			//send event
			try
			{
				String osString = reportSerialiser.serialise(pc);

				eventService.saveEvent("PurgeContentReport", osString);
			}
			catch (Throwable e)
			{
				logger.info("Unable to send PurgeContentReport for Delete Package", e);
			}
		}
	}


	private void sendPurgeMaterial(MayamClientErrorCode opCode, DeleteMaterial action)
	{

		if (opCode == MayamClientErrorCode.SUCCESS)
		{
			PurgeContent pc = new PurgeContent();
			pc.setMaterialID(action.getMaterial().getMaterialID());

			//send event
			try
			{
				String osString = reportSerialiser.serialise(pc);

				eventService.saveEvent("PurgeContentReport", osString);
			}
			catch (Exception e)
			{
				logger.info("Unable to send PurgeContentReport for DeleteMaterial", e);
			}
		}
	}


	private void deleteMaterial(DeleteMaterial action) throws MessageProcessingFailedException
	{

		MayamClientErrorCode result = mayamClient.deleteMaterial(action);
		sendPurgeMaterial(result, action);
		checkResult(result);

	}

	private void deletePackage(DeletePackage action) throws MessageProcessingFailedException
	{

		MayamClientErrorCode result = mayamClient.deletePackage(action);
		sendPurgePackage(result, action);
		checkResult(result);

	}

	/**
	 * Processes a PlaceHoldermessage (which is assumed to have already been validated)
	 * 
	 * @param envelope
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
			eventService.saveEvent("CreateOrUpdateTitle", message);
		}
		else if (isPurgeTitle)
		{
			purgeTitle((PurgeTitle) action);
			eventService.saveEvent("PurgeTitle", message);
		}
		else if (isAddOrUpdateMaterial)
		{
			addOrUpdateMaterial((AddOrUpdateMaterial) action);
			eventService.saveEvent("AddOrUpdateMaterial", message);
		}
		else if (isDeleteMaterial)
		{
			deleteMaterial((DeleteMaterial) action);
			eventService.saveEvent("DeleteMaterial", message);
		}
		else if (isAddOrUpdatePackage)
		{
			addOrUpdatePackage((AddOrUpdatePackage) action);
			eventService.saveEvent("AddOrUpdatePackage", message);
		}
		else if (isDeletePackage)
		{
			deletePackage((DeletePackage) action);
			eventService.saveEvent("DeletePackage", message);
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
		logger.error("Placeholder Agent does not expect non message files, moving to failure folder");
		moveFileToFailureFolder(new File(filePath));
	}

	@Override
	protected boolean shouldArchiveMessages()
	{
		return true;
	}

	@Override
	protected void moveFileToFailureFolder(File file)
	{

		String message;
		try
		{
			if (FilenameUtils.getExtension(file.getAbsolutePath()).toLowerCase().equals("xml"))
			{
				message = FileUtils.readFileToString(file);
			}
			else
			{
				message = file.getAbsolutePath();
			}
		}
		catch (IOException e)
		{
			logger.warn("IOException reading " + file.getAbsolutePath(), e);
			message = file.getAbsolutePath();
		}
		eventService.saveEvent("failed", message);

		super.moveFileToFailureFolder(file);
	}
	
	@Override
	protected void messageValidationFailed(String filePath, MessageValidationResult result)
	{

		logger.warn(String.format(
				"Validation of Placeholder management message %s failed for reason %s",
				FilenameUtils.getName(filePath),
				result.toString()));
		
		String message;
		try{
			message = FileUtils.readFileToString(new File(filePath));
		}
		catch(IOException e){
			logger.warn("IOException reading "+filePath,e);
			message = filePath;
		}
		
		try
		{
			mayamClient.createWFEErrorTaskNoAsset(FilenameUtils.getName(filePath), "Invalid Placeholder Message Received", String.format("Failed to validate %s for reason %s",filePath,result.toString()));
		}
		catch (MayamClientException e)
		{
			logger.error("Failed to create wfe error task",e);
		}
		if(result==MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED){
			eventService.saveEvent("ProtectedPurgeFail",message);
		}
		else{
			eventService.saveEvent("failed",message);
		}
	}

}
