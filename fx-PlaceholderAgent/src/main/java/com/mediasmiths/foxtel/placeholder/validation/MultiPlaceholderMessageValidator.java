package com.mediasmiths.foxtel.placeholder.validation;

import au.com.foxtel.cf.mam.pms.Actions;
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
import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.PackageNotFoundException;
import com.mediasmiths.mayam.validation.MayamValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;

public class MultiPlaceholderMessageValidator extends
		MessageValidator<PlaceholderMessage> {

	private static Logger logger = Logger.getLogger(MultiPlaceholderMessageValidator.class);

	private final MayamClient mayamClient;
	private final MayamValidator mayamValidator;
	private final ChannelProperties channelValidator;

	@Inject
	public MultiPlaceholderMessageValidator(Unmarshaller unmarshaller,
	                                   MayamClient mayamClient, MayamValidator mayamValidator,
	                                   ReceiptWriter receiptWriter, SchemaValidator schemaValidator,
	                                   ChannelProperties channelProperties) throws SAXException {
		super(unmarshaller, receiptWriter, schemaValidator);
		this.mayamClient = mayamClient;
		this.mayamValidator = mayamValidator;
		this.channelValidator = channelProperties;
	}

	/**
	 *
	 * @param pp the pickup location of the file
	 * @param message the placeholder message to be validate
	 * @return IS_VALID if the message container is valid. Correct ID, Sender Id, ok private message data.
	 */
	@Override
	protected MessageValidationResult validateMessage(PickupPackage pp, PlaceholderMessage message) {

		try
		{
			Actions actions = message.getActions();
			String messageID = message.getMessageID();
			String senderID = message.getSenderID();
			Object privateMessageData = message.getPrivateMessageData();

			if (!validateMesageID(pp.getNameForExtension("xml"), messageID)) {
				return MessageValidationResult.INVALID_MESSAGE_ID;
			}

			if (!validateSenderID(senderID)) {
				return MessageValidationResult.INVALID_SENDER_ID;
			}

			if (!validatePrivateMessageData(privateMessageData)) {
				return MessageValidationResult.INVALID_PRIVATE_MESSAGE_DATA;
			}

			return MessageValidationResult.IS_VALID;
		}
		catch (Throwable e)
		{
			logger.error("Unknown validation error: ", e);

			return MessageValidationResult.UNKOWN_VALIDATION_FAILURE;
		}
	}


	public void validateAddOrUpdatePackage(AddOrUpdatePackage action) throws MessageValidationException
	{

		try
		{
			logger.debug("Validating an AddOrUpdatePackage");

			final String titleID = action.getTitleID();

			//reject empty titleIDs
			if (StringUtils.isEmpty(titleID))
			{
				throw new MessageValidationException(MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY);
			}

			if (action.getPackage() != null)
			{
				final String packageID = action.getPackage().getPresentationID();

				//reject empty packageID
				if (StringUtils.isEmpty(packageID))
				{
					throw new MessageValidationException(MessageValidationResult.PACKAGEID_IS_NULL_OR_EMPTY);
				}
			}

			String materialID = action.getPackage().getMaterialID();

			//reject empty materialIDs
			if (StringUtils.isEmpty(materialID))
			{
				throw new MessageValidationException(MessageValidationResult.MATERIALID_IS_NULL_OR_EMPTY);
			}

			boolean materialExists = false;

			// check that material\item for package exists
			try
			{
				materialExists = mayamClient.materialExists(materialID);
			}
			catch (MayamClientException e)
			{
				// catch this exception for logging purposes then throw back up, or
				// should we have a new exception type with e as its cause?
				logger.error(String.format("MayamClientException %s when querying if material %s exists",
				                           e.getErrorcode().toString(),
				                           materialID), e);
				throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
			}

			if (!materialExists)
			{
				logger.error("NO_EXISTING_MATERIAL_FOR_PACKAGE");
				throw new MessageValidationException(MessageValidationResult.NO_EXISTING_MATERIAL_FOR_PACKAGE);
			}

			MessageValidationResult consumerAdviceValid = validateConsumerAdvice(action.getPackage().getConsumerAdvice());

			if (consumerAdviceValid != MessageValidationResult.IS_VALID)
			{
				logger.warn(String.format("Consumer advice \"%s\" fails validation for reason %s",
				                          action.getPackage().getConsumerAdvice(),
				                          consumerAdviceValid));
				throw new MessageValidationException(MessageValidationResult.CONSUMER_ADVICE_INVALID);
			}

			validateMaterialIsForGivenTitle(materialID, titleID);

			validatePackageIsForGivenMaterial(materialID, action.getPackage().getPresentationID());
		}
		catch (MessageValidationException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error("Unknown validation error: ", e);

			throw new MessageValidationException(MessageValidationResult.UNKOWN_VALIDATION_FAILURE);
		}
	}


	private MessageValidationResult validateConsumerAdvice(String consumerAdvice) {

		logger.info("consumer advice not being validated");
		return MessageValidationResult.IS_VALID;

	}

	public void validateDeletePackage(DeletePackage action) throws MessageValidationException
	{
		try
		{
			logger.info("Validating a DeletePackage");

			String packageID = action.getPackage().getPresentationID();

			//reject empty packageIDs
			if (StringUtils.isEmpty(packageID))
			{
				throw new MessageValidationException(MessageValidationResult.PACKAGEID_IS_NULL_OR_EMPTY);
			}

			//reject empty title IDs
			if (StringUtils.isEmpty(action.getTitleID()))
			{
				throw new MessageValidationException(MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY);
			}

			boolean packageProtected = false;
			try
			{
				packageProtected = mayamClient.isTitleOrDescendentsProtected(action.getTitleID());
			}
			catch (MayamClientException e)
			{
				logger.error(String.format("MayamClientException when querying isMaterialForPackageProtected for package %s",
				                           packageID), e);
				throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
			}

			boolean packageExists = false;
			try
			{
				packageExists = mayamClient.packageExists(packageID);
			}
			catch (MayamClientException e)
			{
				logger.error(String.format("MayamClientException when querying packageExists for package %s", packageID), e);
				throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
			}

			if (packageProtected)
			{
				logger.error("PACKAGES_MATERIAL_IS_PROTECTED");
				throw new MessageValidationException(MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED);
			}

			if (!packageExists)
			{
				logger.error("PACKAGE_DOES_NOT_EXIST");
				throw new MessageValidationException(MessageValidationResult.PACKAGE_DOES_NOT_EXIST);
			}

			validatePackageIDisForTitleID(packageID, action.getTitleID());
		}
		catch (MessageValidationException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error("Unknown validation error: ", e);

			throw new MessageValidationException(MessageValidationResult.UNKOWN_VALIDATION_FAILURE);
		}

	}


	public void validateDeleteMaterial(DeleteMaterial action) throws MessageValidationException
	{

		try
		{
			logger.info("Validationg a DeleteMaterial");

			String materialID = action.getMaterial().getMaterialID();

			//reject empty materialIDs
			if (StringUtils.isEmpty(materialID))
			{
				throw new MessageValidationException(MessageValidationResult.MATERIALID_IS_NULL_OR_EMPTY);
			}

			String titleID = action.getTitleID();

			//reject empty titleIDs
			if (StringUtils.isEmpty(titleID))
			{
				throw new MessageValidationException(MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY);
			}

			boolean materialProtected = false;
			try
			{
				materialProtected = mayamClient.isTitleOrDescendentsProtected(titleID);
			}
			catch (MayamClientException e)
			{
				logger.error(String.format("MayamClientException when querying isTitleOrDescendentsProtected for title %s",
				                           titleID), e);
				throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
			}

			boolean materialExists = false;
			try
			{
				materialExists = mayamClient.materialExists(materialID);
			}
			catch (MayamClientException e)
			{
				logger.error(String.format("MayamClientException when querying materialExists for material %s", materialID), e);
				throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
			}

			if (materialProtected)
			{
				logger.error("TITLE_OR_DESCENDENTS_IS_PROTECTED");
				throw new MessageValidationException(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED);
			}
			if (!materialExists)
			{
				logger.error("MATERIAL_DOES_NOT_EXIST");
				throw new MessageValidationException(MessageValidationResult.MATERIAL_DOES_NOT_EXIST);
			}

			validateMaterialIsForGivenTitle(materialID, titleID);
		}
		catch (MessageValidationException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error("Unknown validation error: ", e);

			throw new MessageValidationException(MessageValidationResult.UNKOWN_VALIDATION_FAILURE);
		}

	}

	public void validatePurgeTitle(PurgeTitle action) throws MessageValidationException
	{
		try
		{
			logger.info("Validating a PurgeTitle");

			// 24.1.1.1 Title purge requests
			// check that the title is not marked as protected in ardome
			// check that lower level entries are not procteted, as this should
			// cause the request to fail
			String titleID = action.getTitleID();

			//reject empty titleIDs
			if (StringUtils.isEmpty(titleID))
			{
				throw new MessageValidationException(MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY);
			}

			boolean titleProtected = false;
			try
			{
				titleProtected = mayamClient.isTitleOrDescendentsProtected(titleID);
			}
			catch (MayamClientException e)
			{
				logger.error(String.format("MayamClientException when querying isTitleOrDescendentsProtected for title %s",
				                           titleID), e);

				throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
			}

			boolean titleExists = false;
			try
			{
				titleExists = mayamClient.titleExists(titleID);
			}
			catch (MayamClientException e)
			{
				logger.error(String.format("MayamClientException when querying titleExists for title %s", titleID), e);

				throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
			}

			if (titleProtected)
			{
				logger.error("TITLE_OR_DESCENDENTS_IS_PROTECTED");
				throw new MessageValidationException(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED);
			}

			if (!titleExists)
			{
				logger.error("NO_EXISTING_TITLE_TO_PURGE");
				throw new MessageValidationException(MessageValidationResult.NO_EXISTING_TITLE_TO_PURGE);
			}
		}
		catch (MessageValidationException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error("Unknown validation error: ", e);

			throw new MessageValidationException(MessageValidationResult.UNKOWN_VALIDATION_FAILURE);
		}
	}

	/**
	 * Validates an AddOrUpdateMaterial request, checks material was ordered
	 * before its required by date
	 *
	 * @param action
	 *
	 * @return
	 */
	public void validateAddOrUpdateMaterial(AddOrUpdateMaterial action) throws MessageValidationException
	{
		try
		{
			logger.info("Validating an AddOrUpdateMaterial");

			String materialID = null;

			if (action.getMaterial() != null)
			{
				materialID = action.getMaterial().getMaterialID();

				//reject empty materialIDs
				if (StringUtils.isEmpty(materialID))
				{
					throw new MessageValidationException(MessageValidationResult.MATERIALID_IS_NULL_OR_EMPTY);
				}
			}

			// check if title for material exists
			String titleID = action.getTitleID();
			boolean titleExists = false;

			try
			{
				titleExists = mayamClient.titleExists(titleID);
			}
			catch (MayamClientException e)
			{

				logger.error(String.format("MayamClientException when querying if title %s exists", titleID), e);

				throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
			}

			if (!titleExists)
			{
				logger.warn("Title for material does not exist: " + titleID);
				logger.error("NO_EXISTING_TITLE_FOR_MATERIAL");
				throw new MessageValidationException( MessageValidationResult.NO_EXISTING_TITLE_FOR_MATERIAL);
			}

			validateMaterialIsForGivenTitle(materialID, titleID);
		}
		catch (MessageValidationException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error("Unknown validation error: ", e);

			throw new MessageValidationException(MessageValidationResult.UNKOWN_VALIDATION_FAILURE);
		}
	}

	private void validateMaterialIsForGivenTitle(String materialID, String titleID) throws MessageValidationException
	{

		try
		{
			logger.debug(String.format("Checking that materialID %s is for title %s",materialID,titleID));

			//check that if the material does exist then the title id matches the one specified
			AttributeMap materialAttributes = mayamClient.getMaterialAttributes(materialID);

			if (materialAttributes != null)
			{
				// if material already exists check that the title id matches
				String parentHouseID = materialAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID);

				logger.info(String.format("material with id %s exists with parent %s", materialID, parentHouseID));

				if (!titleID.equals(parentHouseID))
				{
					logger.debug(String.format("{%s} ne {%s}", titleID, parentHouseID));
					logger.error("Material exists but under a different title than specified");
					throw new MessageValidationException(MessageValidationResult.MATERIAL_EXISTS_UNDER_DIFFERENT_TITLE);
				}
			}
		}
		catch (MayamClientException e)
		{
			logger.error("MayamClientFailure: Attempt to validate MaterialID " + materialID + " for TitleID: " + titleID, e);

			throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
		}
	}

	private void validatePackageIsForGivenMaterial(String materialID, String packageID) throws MessageValidationException
	{
		logger.debug(String.format("Checking that packageid %s is for material %s", packageID, materialID));

		try
		{
			SegmentList txpackage = mayamClient.getTxPackage(packageID, materialID);

			if (txpackage != null)
			{
				AttributeMap packageAttributes = txpackage.getAttributeMap();

				// if package already exists, check the materialid matches
				String parentHouseID = packageAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID);

				if (! materialID.equals(parentHouseID))
				{
					logger.debug(String.format("{%s} ne {%s}", packageID, parentHouseID));
					logger.error("Package exists but under a different material than specified");
					throw new MessageValidationException( MessageValidationResult.PACKAGE_EXISTS_UNDER_DIFFERENT_MATERIAL);
				}
			}

		}
		catch (PackageNotFoundException pnfe)
		{
			// package doesnt exist
		}
		catch (MayamClientException e)
		{
			logger.error("MayamClientFailure: Attempting to validate MaterialID: " + materialID + " for PackageID: " + packageID, e);

			throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
		}

	}

	private void validatePackageIDisForTitleID(String packageID, String titleID) throws MessageValidationException
	{
		try{
			//find the package, and take its materialid
			SegmentList txPackage = mayamClient.getTxPackage(packageID);
			String materialID = txPackage.getAttributeMap().getAttribute(Attribute.PARENT_HOUSE_ID);
			logger.debug(String.format("found material id %s for package %s",materialID,packageID));

			validateMaterialIsForGivenTitle(materialID, titleID);
		}
		catch(PackageNotFoundException pnfe)
		{
			logger.trace("package not found");
		}
		catch (MayamClientException e)
		{
			logger.error("Validating package - mayam error", e);
            throw new MessageValidationException(MessageValidationResult.MAYAM_CLIENT_ERROR);
		}

	}


	/**
	 * Validates a CreateOrUpdateTitle request
	 * <p/>
	 * Checks that licence end dates are after licence start dates
	 *
	 * @param action
	 *
	 * @return
	 */
	public void validateCreateOrUpdateTitle(CreateOrUpdateTitle action) throws MessageValidationException
	{
		try
		{

			logger.debug("Validating CreateOrUpdateTitle");


			final String titleID = action.getTitleID();

			//reject empty titleIDs
			if (StringUtils.isEmpty(titleID))
			{
				throw new MessageValidationException(MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY);
			}

			RightsType rights = action.getRights();

			boolean atLeastOneKnownChannel = false;

			for (License l : rights.getLicense())
			{
				XMLGregorianCalendar startDate = l.getLicensePeriod().getStartDate();
				XMLGregorianCalendar endDate = l.getLicensePeriod().getEndDate();

				if (startDate.compare(endDate) == DatatypeConstants.GREATER)
				{
					logger.error("End date before start date");
					logger.error("LICENSE_DATES_NOT_IN_ORDER");
					//print log message but dont reject based on licence information
				}

				Channels channels = l.getChannels();
				if (channels != null)
				{
					for (ChannelType channel : channels.getChannel())
					{
						if (!channelValidator.isTagValid(channel.getChannelTag()))
						{
							logger.warn(String.format("Channel tag unknown '%s' but message wont be rejected because of it",
							                          channel.getChannelTag()));

						}
						else
						{
							atLeastOneKnownChannel = true;
						}
					}
				}
			}

			if (!atLeastOneKnownChannel)
			{
				logger.error("message did not contain any known channels");
				throw new MessageValidationException(MessageValidationResult.CHANNEL_NAME_INVALID);
			}
		}
		catch (MessageValidationException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error("Unknown validation error: ", e);

			throw new MessageValidationException(MessageValidationResult.UNKOWN_VALIDATION_FAILURE);
		}
	}

	private boolean validatePrivateMessageData(Object privateMessageData) {
		// private message data can be of any type
		if (privateMessageData != null) {
			logger.debug("Validating private message data"
			             + privateMessageData.toString());
		} else {
			logger.debug("privateMessageData is null");
		}

		return true;
	}

	private boolean validateSenderID(String senderID) {
		logger.info("Sender id is not validated " + senderID);
		return true;
	}

	private boolean validateMesageID(String messagePath, String messageID) {
		// check if there is already a receipt for this file and warn if it
		// exists

		File receiptFile = new File(getReceiptWriter().receiptPathForMessageID(messagePath,
		                                                                       messageID));
		boolean exists = receiptFile.exists();

		if (exists) {
			logger.warn(String.format("A receipt file already exists for message %s", messageID));
		}

		return true;

	}

	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException
	{ // NOSONAR
		// throwing unchecked exception as hint to users of class that this
		// method is likely to throw ClassCastException

		if (!(unmarshalled instanceof PlaceholderMessage))
		{
			throw new ClassCastException(String.format("unmarshalled type %s is not a PlaceholderMessage",
			                                           unmarshalled.getClass().toString()));
		}

	}

}
