package com.mediasmiths.foxtel.placeholder.processing;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.ChannelType;
import au.com.foxtel.cf.mam.pms.Channels;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.License;
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.Source;
import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailureReason;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.IFilePickup;
import com.mediasmiths.foxtel.agent.validation.MessageValidationException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResultPackage;
import com.mediasmiths.foxtel.ip.common.events.ErrorReport;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.common.events.IBMSDeleteItemFailure;
import com.mediasmiths.foxtel.ip.common.events.ProtectedPurgeFail;
import com.mediasmiths.foxtel.ip.common.events.PurgeMaterial;
import com.mediasmiths.foxtel.ip.common.events.PurgePackage;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.placeholder.validation.MultiPlaceholderMessageValidator;
import com.mediasmiths.foxtel.placeholder.validation.PlaceholderMessageValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Processes placeholder messages taken from a queue
 *
 * @author Mediasmiths Forge
 * @see PlaceholderMessageValidator
 *
 */

public class MultiPlaceholderMessageProcessor extends MessageProcessor<PlaceholderMessage>
{

	private static Logger logger = Logger.getLogger(MultiPlaceholderMessageProcessor.class);

    private final MayamClient mayamClient;

	private final MultiPlaceholderMessageValidator messageValidator;

    @Inject
    public MultiPlaceholderMessageProcessor(
    		IFilePickup filePickup,
    		MultiPlaceholderMessageValidator messageValidator,
            ReceiptWriter receiptWriter,
            Unmarshaller unmarhsaller,
            Marshaller marshaller,
            MayamClient mayamClient,
            EventService eventService)
    {
        super(filePickup, messageValidator, receiptWriter, unmarhsaller, marshaller,eventService);
        this.mayamClient = mayamClient;
	    this.messageValidator = (MultiPlaceholderMessageValidator)messageValidator;
    }

    public void addOrUpdateMaterial(AddOrUpdateMaterial action) throws MessageProcessingFailedException, MessageValidationException
    {
	    messageValidator.validateAddOrUpdateMaterial(action);

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
            }
            sendMaterialOrderStatus(action);

            checkResult(result, action.getClass().getName(),  action.getMaterial().getMaterialID(), action.getTitleID());

        }
        catch (MayamClientException e)
        {
	        logger.error(String.format("MayamClientException querying if material %s exists",
	                                   action.getMaterial().getMaterialID()), e);
	        throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION, e);
        }
	    catch (MessageProcessingFailedException e)
	    {
		    throw e;
	    }
	    catch (Throwable e)
	    {
		    logger.error("Unknown Processing Exception: ", e);

		    throw new MessageProcessingFailedException(MessageProcessingFailureReason.UNKNOWN_PROCESSING_FAILURE);
	    }
    }

    public void addOrUpdatePackage(AddOrUpdatePackage action) throws MessageProcessingFailedException, MessageValidationException
    {

	    messageValidator.validateAddOrUpdatePackage(action);

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
            sendPackageOrderStatus(action);

            checkResult(result, action.getClass().getName(), action.getPackage().getPresentationID(), action.getTitleID());

        }
        catch (MayamClientException e)
        {
	        logger.error(String.format("MayamClientException querying if package %s exists",
	                                   action.getPackage().getPresentationID()), e);
	        throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION, e);
        }
        catch (MessageProcessingFailedException e)
        {
	        throw e;
        }
        catch (Throwable e)
        {
	        logger.error("Unknown Processing Exception: ", e);

	        throw new MessageProcessingFailedException(MessageProcessingFailureReason.UNKNOWN_PROCESSING_FAILURE);
        }
    }

    /**
     * Checks if a MayamClientErrorCode indicated success or not
     *
     * @param result
     * @throws MessageProcessingFailedException
     *             if result != MayamClientErrorCode.SUCCESS
     */
    private void checkResult(MayamClientErrorCode result, String opName, String mediaId, String title) throws MessageProcessingFailedException
    {
        logger.trace("checkResult(" + result + ")");

        if (result == MayamClientErrorCode.SUCCESS)
        {
            logger.info("Action successfully processed");
            logger.info("\n");
        }
        else
        {
	        try
	        {
		        ErrorReport errorReport = new ErrorReport();
		        errorReport.setBmsOp(opName);
		        errorReport.setMediaId(mediaId);
		        errorReport.setStatus(result.toString());

		        if (title==null)
		        {
		            errorReport.setTitle("Unknown in this context.");
		        }
		        else
		        {
		            errorReport.setTitle(title);

		        }
		        eventService.saveEvent("http://www.foxtel.com.au/ip/bms", EventNames.BMS_FAILURE, errorReport);

		        logger.error(String.format("Failed to process action, result was %s", result));
	        }
	        catch (Exception e)
	        {
		        logger.error("Unable to create event error report.", e);
	        }

	        throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION);
        }
    }

    public void createOrUpdateTitle(CreateOrUpdateTitle action) throws MessageValidationException, MessageProcessingFailedException
    {

        messageValidator.validateCreateOrUpdateTitle(action);

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
            sendTitleOrderStatus(action);

            checkResult(result, action.getClass().getName(), action.getTitleID(), action.getTitleDescription().getProgrammeTitle());
        }
        catch (MayamClientException e)
        {
            logger.error(String.format("MayamClientException querying if title %s exists", action.getTitleID()), e);
            throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION, e);
        }
        catch (MessageProcessingFailedException e)
        {
	        throw e;
        }
	    catch (Throwable e)
	    {
		    logger.error("Unknown Processing Exception: ", e);

		    throw new MessageProcessingFailedException(MessageProcessingFailureReason.UNKNOWN_PROCESSING_FAILURE);
	    }
    }

	private void sendMaterialOrderStatus(AddOrUpdateMaterial action)
	{
		try
		{
			com.mediasmiths.foxtel.ip.common.events.AddOrUpdateMaterial addOrUpdateMaterial = new com.mediasmiths.foxtel.ip.common.events.AddOrUpdateMaterial();

			Source source = action.getMaterial().getSource();
			if (source != null && source.getAggregation() != null)
			{
				if (source.getAggregation().getAggregator() != null)
				{
					addOrUpdateMaterial.setAggregatorID(source.getAggregation().getAggregator().getAggregatorID());
				}

				if (source.getAggregation().getOrder() != null)
				{
					addOrUpdateMaterial.setOrderReference(source.getAggregation().getOrder().getOrderReference());
				}

			}

			addOrUpdateMaterial.setRequiredBy(action.getMaterial().getRequiredBy());
			addOrUpdateMaterial.setTitleID(action.getTitleID());
			addOrUpdateMaterial.setMaterialID(action.getMaterial().getMaterialID());

			// send event
			eventService.saveEvent("http://www.foxtel.com.au/ip/bms", EventNames.ADD_OR_UPDATE_MATERIAL, addOrUpdateMaterial);
		}
		catch (Throwable e)
		{
			logger.error("Unable to send event report.", e);
		}
	}

    private void sendTitleOrderStatus(CreateOrUpdateTitle action)
    {

	    try
	    {
		    com.mediasmiths.foxtel.ip.common.events.CreateOrUpdateTitle createOrUpdateTitle = new com.mediasmiths.foxtel.ip.common.events.CreateOrUpdateTitle();

		    List<String> channelsList = getChannels(action.getRights());

		    String channelsString = StringUtils.join(channelsList, ',');
		    createOrUpdateTitle.setChannels(channelsString);
		    createOrUpdateTitle.setTitle(action.getTitleDescription().getProgrammeTitle());
		    createOrUpdateTitle.setTitleID(action.getTitleID());

		    //send event
		    eventService.saveEvent("http://www.foxtel.com.au/ip/bms", EventNames.CREATEOR_UPDATE_TITLE, createOrUpdateTitle);
	    }
	    catch (Throwable e)
	    {
		    logger.error("Unable to send event report.", e);
	    }

    }



    private void sendPurgeTitle(final PurgeTitle action)
    {
	    try
	    {
		    com.mediasmiths.foxtel.ip.common.events.PurgeTitle pt = new com.mediasmiths.foxtel.ip.common.events.PurgeTitle();

		    pt.setTitleID(action.getTitleID());

		    eventService.saveEvent("http://www.foxtel.com.au/ip/bms", EventNames.PURGE_TITLE, pt);
	    }
	    catch (Throwable e)
	    {
		    logger.error("Unable to send event report.", e);
	    }

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

	    try
	    {
		    com.mediasmiths.foxtel.ip.common.events.AddOrUpdatePackage addOrUpdatePackage = new com.mediasmiths.foxtel.ip.common.events.AddOrUpdatePackage();

		    addOrUpdatePackage.setTitleID(action.getTitleID());
		    addOrUpdatePackage.setMaterialID(action.getPackage().getMaterialID());
		    addOrUpdatePackage.setPackageID(action.getPackage().getPresentationID());
		    addOrUpdatePackage.setRequiredBy(action.getPackage().getTargetDate());

		    //send event
		    eventService.saveEvent("http://www.foxtel.com.au/ip/bms", EventNames.ADD_OR_UPDATE_PACKAGE, addOrUpdatePackage);
	    }
	    catch (Throwable e)
	    {
		    logger.error("Unable to send event report.", e);
	    }
    }


    private void sendPurgePackage(MayamClientErrorCode opCode, DeletePackage action)
    {

	    try
	    {
		    if (opCode == MayamClientErrorCode.SUCCESS)
		    {
		        PurgePackage pc = new PurgePackage();
		        pc.setPackageID(action.getPackage().getPresentationID());
		        pc.setTitleID(action.getTitleID());

		        eventService.saveEvent("http://www.foxtel.com.au/ip/bms", EventNames.DELETE_PACKAGE, pc);

		    }
	    }
	    catch (Throwable e)
	    {
		    logger.error("Unable to send event report.", e);
	    }
    }


    private void sendPurgeMaterial(MayamClientErrorCode opCode, DeleteMaterial action)
    {

	    try
	    {
		    if (opCode == MayamClientErrorCode.SUCCESS)
		    {
		        PurgeMaterial pc = new PurgeMaterial();
		        pc.setMaterialID(action.getMaterial().getMaterialID());
		        pc.setTitleID(action.getTitleID());

		        eventService.saveEvent("http://www.foxtel.com.au/ip/bms", EventNames.DELETE_MATERIAL, pc);
		    }
	    }
	    catch (Throwable e)
	    {
		    logger.error("Unable to send event report.", e);
	    }
    }


    public void deleteMaterial(DeleteMaterial action) throws MessageProcessingFailedException, MessageValidationException
    {
        messageValidator.validateDeleteMaterial(action);

	    MayamClientErrorCode result = null;

	    try
	    {
		    result = mayamClient.deleteMaterial(action);

		    sendPurgeMaterial(result, action);

		    checkResult(result, action.getClass().getName(), action.getMaterial().getMaterialID(), action.getTitleID());
	    }
	    catch (MessageProcessingFailedException e)
	    {
		    throw e;
	    }
	    catch (Throwable e)
	    {
		    logger.error("Unknown Processing Exception: ", e);

		    throw new MessageProcessingFailedException(MessageProcessingFailureReason.UNKNOWN_PROCESSING_FAILURE);
	    }

    }

    public void deletePackage(DeletePackage action) throws MessageProcessingFailedException, MessageValidationException
    {
	    messageValidator.validateDeletePackage(action);
	    try
	    {
		    MayamClientErrorCode result = mayamClient.deletePackage(action);
		    sendPurgePackage(result, action);
		    checkResult(result, action.getClass().getName(), action.getPackage().getPresentationID(), action.getTitleID());
	    }
	    catch (MessageProcessingFailedException e)
	    {
		    throw e;
	    }
	    catch (Throwable e)
	    {
		    logger.error("Unknown Processing Exception: ", e);

		    throw new MessageProcessingFailedException(MessageProcessingFailureReason.UNKNOWN_PROCESSING_FAILURE);
	    }
    }



    public void purgeTitle(PurgeTitle action) throws MessageProcessingFailedException, MessageValidationException
    {
	    messageValidator.validatePurgeTitle(action);

	    try
	    {
		    MayamClientErrorCode result = mayamClient.purgeTitle(action);

		    checkResult(result, action.getClass().getName(), action.getTitleID(), null);

		    if (result == MayamClientErrorCode.SUCCESS)
		    {
			    sendPurgeTitle(action);
		    }
	    }
	    catch (MessageProcessingFailedException e)
	    {
		    throw e;
	    }
	    catch (Throwable e)
	    {
		    logger.error("Unknown Processing Exception: ", e);

		    throw new MessageProcessingFailedException(MessageProcessingFailureReason.UNKNOWN_PROCESSING_FAILURE);
	    }
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



	private void sendErrorEvent(
			final MessageValidationResultPackage<PlaceholderMessage> resultPackage,
			final MessageValidationResult result)
	{
		if (result == MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED
				|| result == MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED)
		{
			ProtectedPurgeFail ppf = createPurgeFailedMessage();

			try
			{
				PlaceholderMessage message = resultPackage.getMessage();
				PlaceholderMessage deleteMessage = message;

				logger.info("Delete Message is : " + deleteMessage.toString());

				if (deleteMessage == null)
				{
					logger.error("Delete Message is null");
				}

				Actions actions = deleteMessage.getActions();
				logger.info("Actions are : " + actions.toString());
				if (actions == null)
				{
					logger.error("Actions are null");
				}

				List<Object> actionsList = actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial();
				if (actionsList == null)
				{
					logger.error("Actions List is null");
				}

				Object action = deleteMessage.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);

				String titleID = null;

				if (action instanceof PurgeTitle)
				{
					ppf.setAssetType("EPISODE");

					titleID = ((PurgeTitle) action).getTitleID();
					ppf.setHouseId(titleID);
				}
				else if (action instanceof DeleteMaterial)
				{
					ppf.setAssetType("ITEM");
					titleID = ((DeleteMaterial) action).getTitleID();
					ppf.setHouseId(((DeleteMaterial) action).getMaterial().getMaterialID());
				}
				else if (action instanceof DeletePackage)
				{
					ppf.setAssetType("SEGMENT_LIST");
					titleID = ((DeletePackage) action).getTitleID();
					ppf.setHouseId(((DeletePackage) action).getPackage().getPresentationID());
				}

				if (titleID != null)
				{
					AttributeMap title = mayamClient.getTitle(titleID);
					if (title != null)
					{
						ppf.setTitle(title.getAttributeAsString(Attribute.ASSET_TITLE));
					}
					Set<String> channelsForTitle = mayamClient.getChannelGroupsForTitle(titleID);
					ppf.getChannelGroup().addAll(channelsForTitle);
				}

			}
			catch (MayamClientException e)
			{
				logger.error("error getting channel groups for title", e);
			}
			catch (Exception e1)
			{
				logger.error("Exception thrown while populating ProtectedPurgeFail message", e1);
			}

			eventService.saveEvent("http://www.foxtel.com.au/ip/bms", EventNames.PROTECTED_PURGE_FAIL, ppf);
		}
	}

	private void createErrorTask(final MessageValidationResultPackage<PlaceholderMessage> resultPackage)
	{
		logger.warn(String.format(
				"Validation of Placeholder management message %s failed for reason %s",
				resultPackage.getPp().getRootName(),
				resultPackage.getResult().toString()));

		try
		{
			mayamClient.createWFEErrorTaskNoAsset(
					resultPackage.getPp().getRootName(),
					"Invalid Placeholder Message Received",
					String.format(
							"Failed to validate %s for reason %s",
							resultPackage.getPp().getPickUp("xml").getAbsolutePath(),
							resultPackage.getResult()));
		}
		catch (MayamClientException e)
		{
			logger.error("Failed to create wfe error task", e);
		}
	}

	/**
	 *
	 * @param resultPackage a validation result for a placeholder
	 *
	 *
	 */
	@Override
	protected void messageValidationFailed(MessageValidationResultPackage<PlaceholderMessage> resultPackage)
	{
		MessageValidationResult result = resultPackage.getResult();

		createErrorTask(resultPackage);

		moveFileToFailureFolder(resultPackage.getPp().getPickUp("xml"));

		sendErrorEvent(resultPackage, result);
	}


	/**
	 *
	 * @param envelope orginal pickup placeholder and pickup
	 * @param results a collection (possibly empty) of failed processing results
	 */
	private void messageValidationFailed(MessageEnvelope<PlaceholderMessage> envelope, List<MessageValidationResultPackage<PlaceholderMessage>> results)
	{
		if (results != null && !results.isEmpty())
		{
			for (MessageValidationResultPackage<PlaceholderMessage> result : results)
			{
				createErrorTask(result);
				sendErrorEvent(result, result.getResult());

			}

			moveFileToFailureFolder(envelope.getPickupPackage().getPickUp("xml"));
		}
	}

	/**
	 *
	 * @param envelope the processing envelope
	 * @param errorActions the list (potentially empty) of processing failures
	 *
	 * Post the collection of error messages in processing the list of argument options.
	 *
	 */
	private void messageProcessingFailed(final MessageEnvelope<PlaceholderMessage> envelope, final List<Object> errorActions) throws MessageProcessingFailedException
	{
		if (errorActions != null && !errorActions.isEmpty())
		{
			throw new MessageProcessingFailedException(MessageProcessingFailureReason.UNKNOWN_ACTION);

		}

	}


	private ProtectedPurgeFail createPurgeFailedMessage ()
    {
        ProtectedPurgeFail purgeMessage = new ProtectedPurgeFail();
        purgeMessage.setTime((new Date()).toString());
        return purgeMessage;
    }

    /**
     *
     * @param action  a single placeholder message.
     * @throws MessageProcessingFailedException if item cannot be added.
     * Perform the individual placeholder message operaton.
     */
    private void processAPlaceholderAction(Object action) throws MessageValidationException, MessageProcessingFailedException
    {
        boolean isCreateOrUpdateTitle = (action instanceof CreateOrUpdateTitle);
        boolean isPurgeTitle = (action instanceof PurgeTitle);
        boolean isAddOrUpdateMaterial = (action instanceof AddOrUpdateMaterial);
        boolean isDeleteMaterial = (action instanceof DeleteMaterial);
        boolean isAddOrUpdatePackage = (action instanceof AddOrUpdatePackage);
        boolean isDeletePackage = (action instanceof DeletePackage);

        if (isCreateOrUpdateTitle)
        {
            createOrUpdateTitle((CreateOrUpdateTitle) action);
        }
        else if (isPurgeTitle)
        {
            purgeTitle((PurgeTitle) action);
        }
        else if (isAddOrUpdateMaterial)
        {
            addOrUpdateMaterial((AddOrUpdateMaterial) action);
        }
        else if (isDeleteMaterial)
        {
            deleteMaterial((DeleteMaterial) action);
        }
        else if (isAddOrUpdatePackage)
        {
            addOrUpdatePackage((AddOrUpdatePackage) action);
        }
        else if (isDeletePackage)
        {
            deletePackage((DeletePackage) action);
        }
        else
        {
            logger.fatal("Either there is a new action type or something has gone very wrong");
        }
    }

    /**
     *
     * @param envelope
     * @param message IBMS or BMS placeholder message
     */
    private void processAsIndividualPlaceholderActions(final MessageEnvelope<PlaceholderMessage> envelope, PlaceholderMessage message) throws MessageProcessingFailedException
    {

	    List<Object> errorActions = new ArrayList<Object>();

	    List<MessageValidationResultPackage<PlaceholderMessage>> validationExceptions = new ArrayList<MessageValidationResultPackage<PlaceholderMessage>>();

        for (Object action : message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial())
        {
            try
            {
                processAPlaceholderAction(action);
            }
            catch (MessageValidationException e)
            {
	            logger.error("Placeholder validation error: " + e.result, e);

	            validationExceptions.add(new MessageValidationResultPackage(envelope.getPickupPackage(), message, e.result));

            }
            catch (MessageProcessingFailedException e)
            {
                logger.error("Placeholder Processing error.", e);

	            errorActions.add(action);
            }
        }

	     // process all the validation failed messages if there are any.
	    messageValidationFailed(envelope, validationExceptions);

	     // process all processing exceptions if there are any.
	    messageProcessingFailed(envelope, errorActions);


    }

	/**
     *
     * @param envelope
     * @param message IBMS placeholder message.
     * Process an IBMS placeholder message consisting of a title and list of addMaterial message
     */
    private void processAsIBMSTransaction(final MessageEnvelope<PlaceholderMessage> envelope, PlaceholderMessage message) throws MessageProcessingFailedException
    {
        CreateOrUpdateTitle title = null;
        List<AddOrUpdateMaterial> addMaterialMsgs = new ArrayList<AddOrUpdateMaterial>();

        try
        {
            for (Object action : message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial())
            {

                if (action instanceof CreateOrUpdateTitle)
                {
                    if (title != null) {
                        throw new IllegalArgumentException("Transactional IBMS Placeholder contains multiple CreateOrUpdateTitle actions");
                    }

                    title = (CreateOrUpdateTitle) action;

	                logger.debug("IBMS Title Id: " + title.getTitleID());

                    createOrUpdateTitle(title);
                }
                else if (action instanceof AddOrUpdateMaterial)
                {
                    if (title == null || isTitleMaterialMismatch(title, (AddOrUpdateMaterial) action))
                        throw new IllegalArgumentException("Title ID Mismatch between CreateTitle and AddMaterial Placeholder Actions.");

	                logger.debug("IBMS Material Id: " + ((AddOrUpdateMaterial) action).getMaterial().getMaterialID());

	                addOrUpdateMaterial((AddOrUpdateMaterial) action);

                    addMaterialMsgs.add((AddOrUpdateMaterial) action);
                }
                else
                {
                    throw new IllegalArgumentException("Transactional IBMS Placeholder contains illegal action " + action.getClass().getCanonicalName());
                }

            }
        }
        catch (MessageValidationException e)
        {
	        logger.error("IBMS: Placeholder validation error", e);

	        ibmsTransactionRollback(title, addMaterialMsgs);

	        messageValidationFailed(new MessageValidationResultPackage<PlaceholderMessage>(envelope.getPickupPackage(), message, e.result));

        }
        catch (MessageProcessingFailedException e)
        {

            logger.error("IBMS: Placeholder Processing error.", e);

            ibmsTransactionRollback(title, addMaterialMsgs);

        }


    }

	private boolean isTitleMaterialMismatch(final CreateOrUpdateTitle title, final AddOrUpdateMaterial action)
	{
		if (title == null)
			throw new IllegalArgumentException("Title is null - internal system error");

		if (action == null)
			throw new IllegalArgumentException("Material is null - internal system error");

		return !action.getTitleID().equals(title.getTitleID());
	}

	/**
     *
     * @param title of the IBMS message (potentially null)
     * @param addMaterialMsgs  non null list (potentially empty) of addMaterial messages
     * Roll back by removing the title and addMaterial messages added.
     *
     */
    private void ibmsTransactionRollback(CreateOrUpdateTitle title, List<AddOrUpdateMaterial> addMaterialMsgs) throws MessageProcessingFailedException
    {
        if (title != null)
        {
            logger.debug("Rolling back IBMS messages.");

            for (AddOrUpdateMaterial m : addMaterialMsgs)
            {
                logger.debug("Removing MaterialId: " + m.getMaterial().getMaterialID());

                DeleteMaterial dm = new DeleteMaterial();
                Material mm = new Material();
                mm.setMaterialID(m.getMaterial().getMaterialID());
                dm.setMaterial(mm);
                dm.setTitleID(title.getTitleID());
                try
                {
                    mayamClient.deleteMaterial(dm);
                }
                catch (Exception dmE)
                {
                    logger.error("Failure in IBMS RollBack for materialID: " + mm.getMaterialID(), dmE);
                    IBMSDeleteItemFailure dFails = new IBMSDeleteItemFailure();
                    dFails.setMediaID(mm.getMaterialID());
                    eventService.saveEvent("http://www.foxtel.com.au/ip/bms",
                                           EventNames.IBMS_ROLL_BACK_DELETE_MATERIAL_FAILS, dFails);
                }
            }

            PurgeTitle t = new PurgeTitle();
            t.setTitleID(title.getTitleID());

            try
            {
                mayamClient.purgeTitle(t);
            }
            catch (Exception tdE)
            {
                logger.error("Failure in IBMS RollBack for titleID: " + title.getTitleID(), tdE);
                IBMSDeleteItemFailure dFails = new IBMSDeleteItemFailure();
                dFails.setMediaID(title.getTitleID());
                eventService.saveEvent("http://www.foxtel.com.au/ip/bms", EventNames.IBMS_ROLL_BACK_DELETE_TITLE_FAILS, dFails);
            }
        }
        else
        {
            logger.debug("IBMS Rollback - Nothing to roll back.");
        }

	    throw new MessageProcessingFailedException(MessageProcessingFailureReason.UNKNOWN_ACTION);
    }

    /**
     *
     * @param message BMS or IBMS placeholder message.
     * @return true if this is a message with 1 title message and any number of addMaterial messages
     */
    private boolean isIBMSTransactionalMessage(PlaceholderMessage message)
    {
        if (message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().size() == 1)
        {
            return false;
        }

        String titleId = null;
        for (Object action : message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial())
        {
            if (action instanceof CreateOrUpdateTitle)
            {
                if (titleId != null) return false;

                titleId = ((CreateOrUpdateTitle)action).getTitleID();

            }
            else if (action instanceof AddOrUpdateMaterial)
            {
                if (titleId == null || !((AddOrUpdateMaterial)action).getTitleID().equals(titleId))
                    return false;

            } else
            {
                return false;
            }
        }
        return true;
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

        if (isIBMSTransactionalMessage(message))
        {
            processAsIBMSTransaction(envelope, message);
        }
        else
        {
            processAsIndividualPlaceholderActions(envelope, message);
        }


    }

}
