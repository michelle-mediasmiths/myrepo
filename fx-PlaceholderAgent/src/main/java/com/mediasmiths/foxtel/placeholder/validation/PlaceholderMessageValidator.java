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
import java.util.List;
@Deprecated
public class PlaceholderMessageValidator extends
		MessageValidator<PlaceholderMessage> {

	private static Logger logger = Logger
			.getLogger(PlaceholderMessageValidator.class);

	private final MayamClient mayamClient;
	private final MayamValidator mayamValidator;
	private final ChannelProperties channelValidator;

	@Inject
	public PlaceholderMessageValidator(Unmarshaller unmarshaller,
			MayamClient mayamClient, MayamValidator mayamValidator,
			ReceiptWriter receiptWriter, SchemaValidator schemaValidator,
			ChannelProperties channelProperties) throws SAXException {
		super(unmarshaller, receiptWriter, schemaValidator);
		this.mayamClient = mayamClient;
		this.mayamValidator = mayamValidator;
		this.channelValidator = channelProperties;
	}

	@Override
	protected MessageValidationResult validateMessage(PickupPackage pp, PlaceholderMessage message){

		Actions actions = message.getActions();
		String messageID = message.getMessageID();
		String senderID = message.getSenderID();
		Object privateMessageData = message.getPrivateMessageData();

		if (!validateMesageID(pp.getPickUp("xml").getAbsolutePath(), messageID)) {
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

	public MessageValidationResult validateAddOrUpdatePackage(
			AddOrUpdatePackage action) throws MayamClientException {

		logger.info("Validating an AddOrUpdatePackage");
		
		final String titleID = action.getTitleID();

		//reject empty titleIDs		
		if(StringUtils.isEmpty(titleID)){
			return MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY;
		}
		
		if(action.getPackage() != null){
			final String packageID = action.getPackage().getPresentationID();
		
			//reject empty packageID		
			if(StringUtils.isEmpty(packageID)){
				return MessageValidationResult.PACKAGEID_IS_NULL_OR_EMPTY;
			}
		}
		
		String materialID = action.getPackage().getMaterialID();
		
		//reject empty materialIDs
		if(StringUtils.isEmpty(materialID)){
			return MessageValidationResult.MATERIALID_IS_NULL_OR_EMPTY;
		}
		
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

		MessageValidationResult consumerAdviceValid = validateConsumerAdvice(action
				.getPackage().getConsumerAdvice());

		if (consumerAdviceValid != MessageValidationResult.IS_VALID) {
			logger.warn(String.format(
					"Consumer advice \"%s\" fails validation for reason %s",
					action.getPackage().getConsumerAdvice(),
					consumerAdviceValid));
			return consumerAdviceValid;
		}

		MessageValidationResult materialIDTitleIDMatch = validateMaterialIsForGivenTitle(materialID, titleID);

		if(materialIDTitleIDMatch!= MessageValidationResult.IS_VALID){
			return materialIDTitleIDMatch;
		}

		MessageValidationResult materialIDPackageIDMatch = validatePackageIsForGivenMaterial(materialID,action.getPackage().getPresentationID());
		
		return materialIDPackageIDMatch;
	}


	private MessageValidationResult validateConsumerAdvice(String consumerAdvice) {

		logger.info("consumer advice not being validated");
		return MessageValidationResult.IS_VALID;

	}

	public MessageValidationResult validateDeletePackage(DeletePackage action)
			throws MayamClientException {

		logger.info("Validating a DeletePackage");

		String packageID = action.getPackage().getPresentationID();
		
		//reject empty packageIDs		
		if(StringUtils.isEmpty(packageID)){
			return MessageValidationResult.PACKAGEID_IS_NULL_OR_EMPTY;
		}

		//reject empty title IDs
		if(StringUtils.isEmpty(action.getTitleID())){
			return MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY;
		}
		
		boolean packageProtected = false;
		try {
			packageProtected = mayamClient.isTitleOrDescendentsProtected(action.getTitleID());
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

		return validatePackageIDisForTitleID(packageID,action.getTitleID());
	}


	public MessageValidationResult validateDeleteMaterial(DeleteMaterial action)
			throws MayamClientException {

		logger.info("Validationg a DeleteMaterial");

		String materialID = action.getMaterial().getMaterialID();
		
		//reject empty materialIDs		
		if(StringUtils.isEmpty(materialID)){
			return MessageValidationResult.MATERIALID_IS_NULL_OR_EMPTY;
		}
		
		String titleID = action.getTitleID();
		
		//reject empty titleIDs		
		if(StringUtils.isEmpty(titleID)){
			return MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY;
		}

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
		
		return validateMaterialIsForGivenTitle(materialID, titleID);

	}

	public MessageValidationResult validatePurgeTitle(PurgeTitle action)
			throws MayamClientException {

		logger.info("Validating a PurgeTitle");

		// 24.1.1.1 Title purge requests
		// check that the title is not marked as protected in ardome
		// check that lower level entries are not procteted, as this should
		// cause the request to fail
		String titleID = action.getTitleID();

		//reject empty titleIDs		
		if(StringUtils.isEmpty(titleID)){
			return MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY;
		}
		
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
	public MessageValidationResult validateAddOrUpdateMaterial(
			AddOrUpdateMaterial action) throws MayamClientException {

		logger.info("Validating an AddOrUpdateMaterial");

		String materialID = null;
		
		if(action.getMaterial()!=null){
			materialID = action.getMaterial().getMaterialID();
			
			//reject empty materialIDs		
			if(StringUtils.isEmpty(materialID)){
				return MessageValidationResult.MATERIALID_IS_NULL_OR_EMPTY;
			}
		}
		
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

		if (! titleExists ){
			logger.warn("Title for material does not exist");
			logger.error("NO_EXISTING_TITLE_FOR_MATERIAL");
			return MessageValidationResult.NO_EXISTING_TITLE_FOR_MATERIAL;
		}

		return validateMaterialIsForGivenTitle(materialID, titleID);
	}

	private MessageValidationResult validateMaterialIsForGivenTitle(String materialID, String titleID) throws MayamClientException
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
				return MessageValidationResult.MATERIAL_EXISTS_UNDER_DIFFERENT_TITLE;
			}
		}
		return MessageValidationResult.IS_VALID;
	}

	private MessageValidationResult validatePackageIsForGivenMaterial(String materialID, String packageID)
			throws MayamClientException
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
					return MessageValidationResult.PACKAGE_EXISTS_UNDER_DIFFERENT_MATERIAL;
				}
			}

			return MessageValidationResult.IS_VALID;
		}
		catch (PackageNotFoundException pnfe)
		{
			// package doesnt exist
			return MessageValidationResult.IS_VALID;
		}

	}
	
	private MessageValidationResult validatePackageIDisForTitleID(String packageID, String titleID) throws PackageNotFoundException, MayamClientException
	{
		try{
		//find the package, and take its materialid		
		SegmentList txPackage = mayamClient.getTxPackage(packageID);
		String materialID = txPackage.getAttributeMap().getAttribute(Attribute.PARENT_HOUSE_ID);
		logger.debug(String.format("found material id %s for package %s",materialID,packageID));
		
		return validateMaterialIsForGivenTitle(materialID, titleID);
		}
		catch(PackageNotFoundException pnfe) 
		{
			logger.trace("package not found");
			return MessageValidationResult.IS_VALID;
		}
		
	}

	
	/**
	 * Validates a CreateOrUpdateTitle request
	 * 
	 * Checks that licence end dates are after licence start dates
	 * 
	 * @param action
	 * @return
	 */
	public MessageValidationResult validateCreateOrUpdateTitle(
			CreateOrUpdateTitle action) {
		logger.debug("Validating CreateOrUpdateTitle");


		final String titleID = action.getTitleID();

		//reject empty titleIDs		
		if(StringUtils.isEmpty(titleID)){
			return MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY;
		}
		
		RightsType rights = action.getRights();
		
		boolean atLeastOneKnownChannel = false;

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
						logger.warn(String
								.format("Channel tag unknown '%s' but message wont be rejected because of it",
										channel.getChannelTag()));
						
					}
					else{
						atLeastOneKnownChannel = true;
					}
				}
			}
		}
		
		if(! atLeastOneKnownChannel){
			logger.error("message did not contain any known channels");
			return MessageValidationResult.CHANNEL_NAME_INVALID;
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
