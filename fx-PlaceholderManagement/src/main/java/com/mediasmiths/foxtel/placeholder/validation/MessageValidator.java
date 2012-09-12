package com.mediasmiths.foxtel.placeholder.validation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.License;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import au.com.foxtel.cf.mam.pms.RightsType;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.placeholder.receipt.ReceiptWriter;
import com.mediasmiths.foxtel.xmlutil.SchemaValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class MessageValidator {

	private final static String SCHEMA_PATH = "PlaceholderManagement.xsd";

	private static Logger logger = Logger.getLogger(MessageValidator.class);

	// an unmarshaller for turning xml files into objects
	private final Unmarshaller unmarhsaller;
	// schema will be used for validating files against
	// PlaceHolderManagement.xsd
	private final SchemaValidator schemaValidator;
	private final MayamClient mayamClient;
	
	private final ReceiptWriter receiptWriter;

	@Inject
	public MessageValidator(Unmarshaller unmarshaller, MayamClient mayamClient, ReceiptWriter receiptWriter)
			throws SAXException {
		this.unmarhsaller = unmarshaller;
		this.schemaValidator = new SchemaValidator(SCHEMA_PATH);
		this.mayamClient = mayamClient;
		this.receiptWriter=receiptWriter;
	}

	/**
	 * Checks the structure of a given xml file against the xsd
	 * 
	 * @param filepath
	 * @return pass
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	private boolean againstXSD(String filepath) throws SAXException,
			ParserConfigurationException, IOException {
		return schemaValidator.isValid(new File(filepath));
	}

	/**
	 * Validates an xml file according to the rules
	 * 
	 * @param filepath
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws MayamClientException
	 */
	public MessageValidationResult validateFile(String filepath)
			throws SAXException, ParserConfigurationException, IOException,
			MayamClientException {

		// first check the xml file conforms to the schema
		boolean againstXSD = againstXSD(filepath);

		if (!againstXSD) {
			return MessageValidationResult.FAILS_XSD_CHECK;
		}

		// xml file has valid schema, unmarshall then continue validation
		PlaceholderMessage message = null;

		try {
			message = (PlaceholderMessage) unmarshallFile(new File(filepath));

		} catch (JAXBException e) {
			logger.fatal("Failed to unmarshall file " + filepath
					+ " that had validated against schema");
			return MessageValidationResult.FAILED_TO_UNMARSHALL;
		} catch (ClassCastException cce) {
			logger.fatal("Unmarshalled file from placeholder management schema that was not a PlaceholderMessage");
			return MessageValidationResult.UNEXPECTED_TYPE;
		}

		return validatePlaceHolderMessage(message);

	}

	private MessageValidationResult validatePlaceHolderMessage(
			PlaceholderMessage message) throws MayamClientException {

		Actions actions = message.getActions();
		String messageID = message.getMessageID();
		String senderID = message.getSenderID();
		Object privateMessageData = message.getPrivateMessageData();

		if (!validateMesageID(messageID)) {
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
			} else {
				logger.warn("Message Actions did not validate");
			}
			return validateActions;
			
		} catch (Exception e) {
			if(!(e instanceof MayamClientException)){
				logger.warn("unknown validation failure!", e);
				return MessageValidationResult.UNKOWN_VALIDATION_FAILURE;
			}
			else{
				throw (MayamClientException) e;
			}
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

		if (isCreateOrUpdateTitle)
			return validateCreateOrUpdateTitle((CreateOrUpdateTitle) action);
		else if (isPurgeTitle)
			return validatePurgeTitle((PurgeTitle) action);
		else if (isAddOrUpdateMaterial)
			return validateAddOrUpdateMaterial((AddOrUpdateMaterial) action);
		else if (isDeleteMaterial)
			return validateDeleteMaterial((DeleteMaterial) action);
		else if (isAddOrUpdatePackage)
			return validateAddOrUpdatePackage((AddOrUpdatePackage) action);
		else if (isDeletePackage)
			return validateDeletePackage((DeletePackage) action);
		else {
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
			logger.error("No existing material for requested package");
			return MessageValidationResult.NO_EXISTING_MATERIAL_FOR_PACKAGE;
		}

		// TODO validate consumer advice (command seperated list of characters);
		logger.warn("No validation of consumer advice has taken place");

		return MessageValidationResult.IS_VALID;
	}

	private MessageValidationResult validateDeletePackage(DeletePackage action)
			throws MayamClientException {
		
		logger.info("Validating a DeletePackage");
		
		// 24.1.1.3 Version purge requests
		// check that the parent item in ardome is not flagged as
		String packageID = action.getPackage().getPresentationID();

		boolean materialForPackageProtected = false;
		try {
			materialForPackageProtected = mayamClient
					.isMaterialForPackageProtected(packageID);
		} catch (MayamClientException e) {
			logger.error(
					String.format(
							"MayamClientException when querying isMaterialForPackageProtected for package %s",
							packageID), e);
			throw e;
		}

		if (materialForPackageProtected) {
			return MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED;
		}

		return MessageValidationResult.IS_VALID;
	}

	private MessageValidationResult validateDeleteMaterial(DeleteMaterial action) {

		logger.info("Validating a DeleteMaterial");
		// 24.1.1.2 Master purge requests

		// TODO : do we check if the material is marked as protected as with the
		// other delete requests? its not specified
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

		if (titleProtected) {
			return MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED;
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

			XMLGregorianCalendar orderCreated = action.getMaterial()
					.getSource().getAggregation().getOrder().getOrderCreated();
			XMLGregorianCalendar requiredBy = action.getMaterial()
					.getRequiredBy();

			if (orderCreated.compare(requiredBy) == DatatypeConstants.GREATER) {
				logger.warn("Required by date is before order created date!");
				return MessageValidationResult.ORDER_CREATED_AND_REQUIREDBY_DATES_NOT_IN_ORDER;
			}
		} else {
			logger.warn("Title for material does not exist");
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
				return MessageValidationResult.LICENCE_DATES_NOT_IN_ORDER;
			}
		}

		// TODO : complete validation of CreateOrUpdateTitle action
		return MessageValidationResult.IS_VALID;
	}

	private boolean validatePrivateMessageData(Object privateMessageData) {
		// private message data can be of any type
		return true;
	}

	private boolean validateSenderID(String senderID) {
		// TODO implement validateSenderID
		return true;
	}

	private boolean validateMesageID(String messageID) {
		//TODO : check there is not already a receipt for this file
		
		File receiptFile = new File(receiptWriter.receiptPathForMessageID(messageID));
		boolean exists= receiptFile.exists();
		
		if(exists){
			logger.warn(String.format("A recipt file already exists for message %s", messageID));
		}
		
		return (! exists);
		
		
	}

	private Object unmarshallFile(File xml) throws JAXBException {
		Object unmarshalled = unmarhsaller.unmarshal(xml);
		logger.debug(String.format("unmarshalled object of type %s",
				unmarshalled.getClass().toString()));
		return unmarshalled;
	}

}
