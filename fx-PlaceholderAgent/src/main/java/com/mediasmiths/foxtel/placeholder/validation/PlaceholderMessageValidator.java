package com.mediasmiths.foxtel.placeholder.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

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
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.placeholder.validation.channels.ChannelValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.validation.MayamValidator;

public class PlaceholderMessageValidator extends
		MessageValidator<PlaceholderMessage> {

	private static Logger logger = Logger
			.getLogger(PlaceholderMessageValidator.class);

	private final MayamClient mayamClient;
	private final MayamValidator mayamValidator;
	private final ChannelValidator channelValidator;

	@Inject
	public PlaceholderMessageValidator(Unmarshaller unmarshaller,
			MayamClient mayamClient, MayamValidator mayamValidator,
			ReceiptWriter receiptWriter, SchemaValidator schemaValidator,
			ChannelValidator channelValidator) throws SAXException {
		super(unmarshaller, receiptWriter, schemaValidator);
		this.mayamClient = mayamClient;
		this.mayamValidator = mayamValidator;
		this.channelValidator = channelValidator;
	}

	@Override
	protected MessageValidationResult validateMessage(String messagePath,PlaceholderMessage message) {

		Actions actions = message.getActions();
		String messageID = message.getMessageID();
		String senderID = message.getSenderID();
		Object privateMessageData = message.getPrivateMessageData();

		if (!validateMesageID(messagePath, messageID)) {
			return MessageValidationResult.INVALID_MESSAGE_ID;
		}

		if (!validateSenderID(senderID)) {
			return MessageValidationResult.INVALID_SENDER_ID;
		}

		if (!validatePrivateMessageData(privateMessageData)) {
			return MessageValidationResult.INVALID_PRIVATE_MESSAGE_DATA;
		}

		try {
			MessageValidationResult validateActions = validateActions(actions);

			if (validateActions == MessageValidationResult.IS_VALID) {
				logger.info("PlaceholderMessage validated");
				logger.info("\n");
			} else {
				logger.warn("Message Actions did not validate");
			}
			return validateActions;

		} catch (MayamClientException e) {
			logger.error("Mayam Client error", e);
			return MessageValidationResult.MAYAM_CLIENT_ERROR;
		} catch (Exception e) {
			logger.error("unknown validation failure!", e);
			return MessageValidationResult.UNKOWN_VALIDATION_FAILURE;
		}
	}

	/**
	 * Validates that only single action has been requested and that action is
	 * valid
	 * 
	 * @param actions
	 * @return
	 * @throws MayamClientException
	 */
	private MessageValidationResult validateActions(Actions actions)
			throws MayamClientException {

		List<Object> actionList = actions
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial();

		if (actionList.size() != 1) {
			logger.error("Actions element contained multiple actions");
			// MD04.1 - Interconnect to Placeholder Management Interface
			// V3.0.doc :
			// 2.1.2 Each XML file will contain a single message of one of the
			// following nine types
			return MessageValidationResult.ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS;
		}

		return validateAction(actionList.get(0));

	}

	private MessageValidationResult validateAction(Object action)
			throws MayamClientException {

		boolean isCreateOrUpdateTitle = (action instanceof CreateOrUpdateTitle);
		boolean isPurgeTitle = (action instanceof PurgeTitle);
		boolean isAddOrUpdateMaterial = (action instanceof AddOrUpdateMaterial);
		boolean isDeleteMaterial = (action instanceof DeleteMaterial);
		boolean isAddOrUpdatePackage = (action instanceof AddOrUpdatePackage);
		boolean isDeletePackage = (action instanceof DeletePackage);

		if (isCreateOrUpdateTitle) {
			return validateCreateOrUpdateTitle((CreateOrUpdateTitle) action);
		} else if (isPurgeTitle) {
			return validatePurgeTitle((PurgeTitle) action);
		} else if (isAddOrUpdateMaterial) {
			return validateAddOrUpdateMaterial((AddOrUpdateMaterial) action);
		} else if (isDeleteMaterial) {
			return validateDeleteMaterial((DeleteMaterial) action);
		} else if (isAddOrUpdatePackage) {
			return validateAddOrUpdatePackage((AddOrUpdatePackage) action);
		} else if (isDeletePackage) {
			return validateDeletePackage((DeletePackage) action);
		} else {
			logger.error("Supplied action is not an action type");
			return MessageValidationResult.UNEXPECTED_TYPE;
		}

	}

	private MessageValidationResult validateAddOrUpdatePackage(
			AddOrUpdatePackage action) throws MayamClientException {

		logger.info("Validating an AddOrUpdatePackage");
		String materialID = action.getPackage().getMaterialID();
		boolean materialExists = false;

		// check that material\item for package exists
		try {
			materialExists = mayamClient.materialExists(materialID);
		} catch (MayamClientException e) {
			// catch this exception for logging purposes then throw back up, or
			// should we have a new exception type with e as its cause?
			logger.error(
					String.format(
							"MayamClientException %s when querying if material %s exists",
							e.getErrorcode().toString(), materialID), e);
			throw e;
		}

		if (!materialExists) {
			logger.error("NO_EXISTING_MATERIAL_FOR_PACKAGE");
			return MessageValidationResult.NO_EXISTING_MATERIAL_FOR_PACKAGE;
		}

//		String presentationFormat = action.getPackage().getPresentationFormat()
//				.toString();
//		ArrayList<String> channelTags = mayamClient
//				.getChannelLicenseTagsForMaterial(materialID);
//
//		for (String channelTag : channelTags) {
//
//			// TODO read this check once we have aversion of mayam that will
//			// persist things!
//
//			// if (!channelValidator.isValidFormatForTag(channelTag,
//			// presentationFormat)) {
//			// logger.error("Presentation Format of package does not match that of associated channel");
//			// return MessageValidationResult.PACKAGE_INVALID_FORMAT;
//			// }
//		}

		// TODO read once mayam persists things
		// XMLGregorianCalendar targetDate =
		// action.getPackage().getTargetDate();
		// if (!mayamValidator.validateMaterialBroadcastDate(targetDate,
		// materialID)) {
		// logger.error("Intended target date of package "+action.getPackage().getPresentationID()+" is not within valid licensed dates");
		// }

		MessageValidationResult consumerAdviceValid = validateConsumerAdvice(action
				.getPackage().getConsumerAdvice());

		if (consumerAdviceValid != MessageValidationResult.IS_VALID) {
			logger.warn(String.format(
					"Consumer advice \"%s\" fails validation for reason %s",
					action.getPackage().getConsumerAdvice(),
					consumerAdviceValid));
			return consumerAdviceValid;
		}

		return MessageValidationResult.IS_VALID;
	}

	private MessageValidationResult validateConsumerAdvice(String consumerAdvice) {

		// TODO match consumer advice with regex [ADHLNSV,]* ?
		logger.warn("consumer advice not being validated");
		return MessageValidationResult.IS_VALID;

	}

	private MessageValidationResult validateDeletePackage(DeletePackage action)
			throws MayamClientException {

		logger.info("Validating a DeletePackage");

		String packageID = action.getPackage().getPresentationID();

		boolean packageProtected = false;
		try {
			packageProtected = mayamClient
					.isMaterialForPackageProtected(packageID);
		} catch (MayamClientException e) {
			logger.error(
					String.format(
							"MayamClientException when querying isMaterialForPackageProtected for package %s",
							packageID), e);
			throw e;
		}

		boolean packageExists = false;
		try {
			packageExists = mayamClient.packageExists(packageID);
		} catch (MayamClientException e) {
			logger.error(
					String.format(
							"MayamClientException when querying packageExists for package %s",
							packageID), e);
			throw e;
		}

		if (packageProtected) {
			logger.error("PACKAGES_MATERIAL_IS_PROTECTED");
			return MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED;
		}

		if (!packageExists) {
			logger.error("PACKAGE_DOES_NOT_EXIST");
			return MessageValidationResult.PACKAGE_DOES_NOT_EXIST;
		}

		return MessageValidationResult.IS_VALID;
	}

	private MessageValidationResult validateDeleteMaterial(DeleteMaterial action)
			throws MayamClientException {

		logger.info("Validationg a DeleteMaterial");

		String materialID = action.getMaterial().getMaterialID();
		String titleID = action.getTitleID();
		boolean materialProtected = false;
		try {
			materialProtected = mayamClient
					.isTitleOrDescendentsProtected(titleID);
		} catch (MayamClientException e) {
			logger.error(
					String.format(
							"MayamClientException when querying isTitleOrDescendentsProtected for title %s",
							titleID), e);
			throw e;
		}

		boolean materialExists = false;
		try {
			materialExists = mayamClient.materialExists(materialID);
		} catch (MayamClientException e) {
			logger.error(
					String.format(
							"MayamClientException when querying materialExists for material %s",
							materialID), e);
			throw e;
		}

		if (materialProtected) {
			logger.error("TITLE_OR_DESCENDENTS_IS_PROTECTED");
			return MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED;
		}
		if (!materialExists) {
			logger.error("MATERIAL_DOES_NOT_EXIST");
			return MessageValidationResult.MATERIAL_DOES_NOT_EXIST;
		}

		return MessageValidationResult.IS_VALID;

	}

	private MessageValidationResult validatePurgeTitle(PurgeTitle action)
			throws MayamClientException {

		logger.info("Validating a PurgeTitle");

		// 24.1.1.1 Title purge requests
		// check that the title is not marked as protected in ardome
		// check that lower level entries are not procteted, as this should
		// cause the request to fail
		String titleID = action.getTitleID();

		boolean titleProtected = false;
		try {
			titleProtected = mayamClient.isTitleOrDescendentsProtected(titleID);
		} catch (MayamClientException e) {
			logger.error(
					String.format(
							"MayamClientException when querying isTitleOrDescendentsProtected for title %s",
							titleID), e);
			throw e;
		}

		boolean titleExists = false;
		try {
			titleExists = mayamClient.titleExists(titleID);
		} catch (MayamClientException e) {
			logger.error(
					String.format(
							"MayamClientException when querying titleExists for title %s",
							titleID), e);
			throw e;
		}

		if (titleProtected) {
			logger.error("TITLE_OR_DESCENDENTS_IS_PROTECTED");
			return MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED;
		}

		if (!titleExists) {
			logger.error("NO_EXISTING_TITLE_TO_PURGE");
			return MessageValidationResult.NO_EXISTING_TITLE_TO_PURGE;
		}

		return MessageValidationResult.IS_VALID;

	}

	/**
	 * Validates an AddOrUpdateMaterial request, checks material was ordered
	 * before its required by date
	 * 
	 * @param action
	 * @return
	 */
	private MessageValidationResult validateAddOrUpdateMaterial(
			AddOrUpdateMaterial action) throws MayamClientException {

		logger.info("Validating an AddOrUpdateMaterial");

		// check if title for material exists
		String titleID = action.getTitleID();
		boolean titleExists = false;

		try {
			titleExists = mayamClient.titleExists(titleID);
		} catch (MayamClientException e) {
			// catch this exception for logging purposes then throw back up, or
			// should we have a new exception type with e as its cause?
			logger.error(String.format(
					"MayamClientException when querying if title %s exists",
					titleID), e);
			throw e;
		}

		if (titleExists) {

//			if (action.getMaterial().getSource().getAggregation() != null) {
//				XMLGregorianCalendar orderCreated = action.getMaterial()
//						.getSource().getAggregation().getOrder()
//						.getOrderCreated();
//				XMLGregorianCalendar requiredBy = action.getMaterial()
//						.getRequiredBy();
//
//				if (orderCreated.compare(requiredBy) == DatatypeConstants.GREATER) {
//					logger.warn("Required by date is before order created date!");
//					logger.error("ORDER_CREATED_AND_REQUIREDBY_DATES_NOT_IN_ORDER");
//					return MessageValidationResult.ORDER_CREATED_AND_REQUIREDBY_DATES_NOT_IN_ORDER;
//				}
//			}
		} else {
			logger.warn("Title for material does not exist");
			logger.error("NO_EXISTING_TITLE_FOR_MATERIAL");
			return MessageValidationResult.NO_EXISTING_TITLE_FOR_MATERIAL;
		}

		return MessageValidationResult.IS_VALID;
	}

	/**
	 * Validates a CreateOrUpdateTitle request
	 * 
	 * Checks that licence end dates are after licence start dates
	 * 
	 * @param action
	 * @return
	 */
	private MessageValidationResult validateCreateOrUpdateTitle(
			CreateOrUpdateTitle action) {
		logger.debug("Validating CreateOrUpdateTitle");

		RightsType rights = action.getRights();

		for (License l : rights.getLicense()) {
			XMLGregorianCalendar startDate = l.getLicensePeriod()
					.getStartDate();
			XMLGregorianCalendar endDate = l.getLicensePeriod().getEndDate();

			if (startDate.compare(endDate) == DatatypeConstants.GREATER) {
				logger.error("End date before start date");
				logger.error("LICENSE_DATES_NOT_IN_ORDER");
				//print log message but dont reject based on licence information
			}

			Channels channels = l.getChannels();
			if (channels != null) {
				for (ChannelType channel : channels.getChannel()) {
					if (!channelValidator.isTagValid(
							channel.getChannelTag())) {
						logger.error(String
								.format("Channel tag invalid 's%'",
										channel.getChannelTag()));
						return MessageValidationResult.CHANNEL_NAME_INVALID;
					}
				}
			}

			try {
				if (mayamClient.titleExists(action.getTitleID())) {
					
					logger.debug("title ID is " + action.getTitleID());
					
					MayamValidator mayamValidator = mayamClient.getValidator();
					if (!mayamValidator.validateTitleBroadcastDate(
							action.getTitleID(), startDate, endDate)) {
						logger.error("License date for title did not valid for one or more packages");
						return MessageValidationResult.TITLE_TARGET_DATE_LICENSE_INVALID;
					}
					
				}
			} catch (MayamClientException e) {
				logger.error("Exception when validating license periods in Mayam",e);
				//exception is logged but we dont want to reject a placeholder message just because of licence info
			}
		}

		String channelTag = action.getRights().getLicense().get(0)
				.getChannels().getChannel().get(0).getChannelTag();
		String channelName = action.getRights().getLicense().get(0)
				.getChannels().getChannel().get(0).getChannelName();
		if (channelTag != null) {
			if ((channelTag.equals("UNKNOWN_CHANNEL_TAG"))
					|| (channelName.equals("UNKNOWN_CHANNEL_NAME"))) {
				logger.error("UNKOWN_CHANNEL");
				return MessageValidationResult.UNKOWN_CHANNEL;
			}
		}

		return MessageValidationResult.IS_VALID;
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
		logger.debug("Validating sender id" + senderID);
		// TODO is any validation of this field required?
		return true;
	}

	private boolean validateMesageID(String messagePath, String messageID) {
		// check if there is already a receipt for this file and warn if it
		// exists

		File receiptFile = new File(getReceiptWriter().receiptPathForMessageID(messagePath,
				messageID));
		boolean exists = receiptFile.exists();

		if (exists) {
			logger.warn(String.format(
					"A recipt file already exists for message %s", messageID));
		}

		return true;

	}

	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException { // NOSONAR
		// throwing unchecked exception as hint to users of class that this
		// method is likely to throw ClassCastException

		if (!(unmarshalled instanceof PlaceholderMessage)) {
			throw new ClassCastException(String.format(
					"unmarshalled type %s is not a PlaceholderMessage",
					unmarshalled.getClass().toString()));
		}

	}

}
