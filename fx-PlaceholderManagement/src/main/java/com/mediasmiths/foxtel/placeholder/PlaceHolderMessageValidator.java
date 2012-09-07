package com.mediasmiths.foxtel.placeholder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
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

import com.mediasmiths.foxtel.xmlutil.SchemaValidator;
import com.mediasmiths.mayam.MayamClient;

public class PlaceHolderMessageValidator {

	private final static String SCHEMA_PATH = "PlaceholderManagement.xsd";
	
	private static Logger logger = Logger
			.getLogger(PlaceHolderMessageValidator.class);

	// an unmarshaller for turning xml files into objects
	private final Unmarshaller unmarhsaller;
	// schema will be used for validating files against
	// PlaceHolderManagement.xsd
	private final SchemaValidator schemaValidator;
	private final MayamClient mayamClient;

	public PlaceHolderMessageValidator(Unmarshaller unmarshaller, MayamClient mayamClient)
			throws SAXException {
		this.unmarhsaller = unmarshaller;
		this.schemaValidator = new SchemaValidator(SCHEMA_PATH);
		this.mayamClient = mayamClient;
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
	 */
	public PlaceHolderMessageValidationResult validateFile(String filepath)
			throws SAXException, ParserConfigurationException, IOException {

		// first check the xml file conforms to the schema
		boolean againstXSD = againstXSD(filepath);

		if (!againstXSD) {
			return PlaceHolderMessageValidationResult.FAILS_XSD_CHECK;
		}

		// xml file has valid schema, unmarshall then continue validation
		PlaceholderMessage message = null;

		try {
			message = (PlaceholderMessage) unmarshallFile(new File(filepath));

		} catch (JAXBException e) {
			logger.fatal("Failed to unmarshall file " + filepath
					+ " that had validated against schema");
			return PlaceHolderMessageValidationResult.FAILED_TO_UNMARSHALL;
		} catch (ClassCastException cce) {
			logger.fatal("Unmarshalled file from placeholder management schema that was not a PlaceholderMessage");
			return PlaceHolderMessageValidationResult.UNEXPECTED_TYPE;
		}

		return validatePlaceHolderMessage(message);

	}

	private PlaceHolderMessageValidationResult validatePlaceHolderMessage(
			PlaceholderMessage message) {

		Actions actions = message.getActions();
		String messageID = message.getMessageID();
		String senderID = message.getSenderID();
		Object privateMessageData = message.getPrivateMessageData();

		// TODO currently returns on first validation failure, should we keep
		// going so we can report any other failures?

		if (!validateMesageID(messageID)) {
			return PlaceHolderMessageValidationResult.INVALID_MESSAGE_ID;
		}

		if (!validateSenderID(senderID)) {
			return PlaceHolderMessageValidationResult.INVALID_SENDER_ID;
		}

		if (!validatePrivateMessageData(privateMessageData)) {
			return PlaceHolderMessageValidationResult.INVALID_PRIVATE_MESSAGE_DATA;
		}

		if (validateActions(actions) ==PlaceHolderMessageValidationResult.IS_VALID ) {			
			//TODO need a means of bubbling up inner failure reasons?
			return PlaceHolderMessageValidationResult.INVALID_ACTIONS;
		}

		logger.info("PlaceholderMessage validated");
		return PlaceHolderMessageValidationResult.IS_VALID;
	}

	/**
	 * Validates that only single action has been requested and that action is valid
	 * @param actions
	 * @return
	 */
	private PlaceHolderMessageValidationResult validateActions(Actions actions) {

		List<Object> actionList = actions
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial();

		if (actionList.size() != 1) {
			logger.error("Actions element contained multiple actions");
			// 2.1.2 Each XML file will contain a single message of one of the
			// following nine types
			return PlaceHolderMessageValidationResult.ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS;
		}

		return validateAction(actionList.get(0));

	}

	private PlaceHolderMessageValidationResult validateAction(Object action) {

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
			return PlaceHolderMessageValidationResult.UNEXPECTED_TYPE;
		}

	}

	private PlaceHolderMessageValidationResult validateAddOrUpdatePackage(
			AddOrUpdatePackage action) {
		
		//TODO check corresponding item exists in ardome
		boolean itemExists = true;
		
		if(!itemExists){
			logger.error("No existing material for requested package");
			return PlaceHolderMessageValidationResult.NO_EXISTING_MATERIAL_FOR_PACKAGE;
		}
		
		return PlaceHolderMessageValidationResult.IS_VALID;
	}

	
	private PlaceHolderMessageValidationResult validateDeletePackage(
			DeletePackage action) {
		//24.1.1.3 Version purge requests		
		//TODO : check that the parent item in ardome is not flagged as protected
		return PlaceHolderMessageValidationResult.IS_VALID;
	}
	
	private PlaceHolderMessageValidationResult validateDeleteMaterial(
			DeleteMaterial action) {
		
		//24.1.1.2 Master purge requests		
		// TODO : check that the material and lower level entries is not marked as protected in ardome? (need clarification)
		return PlaceHolderMessageValidationResult.IS_VALID;
	}
	
	private PlaceHolderMessageValidationResult validatePurgeTitle(
			PurgeTitle action) {
		
		//24.1.1.1 Title purge requests		
		// TODO check that the title is not marked as protected in ardome
		// TODO check that lower level entries are not procteted, as this should cause the request to fail
		return PlaceHolderMessageValidationResult.IS_VALID;
	}

	/**
	 * Validates an AddOrUpdateMaterial request, checks material was ordered before its required by date
	 * 
	 * @param action
	 * @return
	 */
	private PlaceHolderMessageValidationResult validateAddOrUpdateMaterial(
			AddOrUpdateMaterial action) {
		
		// TODO check title exists for the material
		boolean titleExists = true;
		
		if (titleExists) {
			
			XMLGregorianCalendar orderCreated = action.getMaterial().getSource().getAggregation().getOrder().getOrderCreated();
			XMLGregorianCalendar requiredBy = action.getMaterial().getRequiredBy();
			
			if (orderCreated.compare(requiredBy) == DatatypeConstants.LESSER) {
				logger.error("Required by date is before order created date!");
				return PlaceHolderMessageValidationResult.ORDER_CREATED_AND_REQUIREDBY_DATES_NOT_IN_ORDER;
			}
		} else {
			logger.error("Title for material does not exist");
			return PlaceHolderMessageValidationResult.NO_EXISTING_TITLE_FOR_MATERIAL;
		}

		return PlaceHolderMessageValidationResult.IS_VALID;
	}

	/**
	 * Validates a CreateOrUpdateTitle request
	 * 
	 * Checks that licence end dates are after licence start dates
	 * 
	 * @param action
	 * @return
	 */
	private PlaceHolderMessageValidationResult validateCreateOrUpdateTitle(
			CreateOrUpdateTitle action) {
		logger.debug("Validating CreateOrUpdateTitle");

		RightsType rights = action.getRights();

		for (License l : rights.getLicense()) {
			XMLGregorianCalendar startDate = l.getLicensePeriod()
					.getStartDate();
			XMLGregorianCalendar endDate = l.getLicensePeriod().getEndDate();

			if (startDate.compare(endDate) == DatatypeConstants.LESSER) {
				logger.error("End date before start date");
				return PlaceHolderMessageValidationResult.LICENCE_DATES_NOT_IN_ORDER;
			}
		}

		// TODO : complete validation of CreateOrUpdateTitle action
		return PlaceHolderMessageValidationResult.IS_VALID;
	}

	private boolean validatePrivateMessageData(Object privateMessageData) {
		// TODO implement validatePrivateMessageData
		return true;
	}

	private boolean validateSenderID(String senderID) {
		// TODO implement validateSenderID
		return true;
	}

	private boolean validateMesageID(String messageID) {
		// TODO implement validateMesageID
		return true;
	}

	private Object unmarshallFile(File xml) throws JAXBException {
		Object unmarshalled = unmarhsaller.unmarshal(xml);
		logger.debug(String.format("unmarshalled object of type %s",
				unmarshalled.getClass().toString()));
		return unmarshalled;
	}

	public static void main(String[] args) throws SAXException, IOException,
			ParserConfigurationException, JAXBException {

		String filepath = "/Users/alisonboal/Documents/Foxtel/XMLTests/test30_createTitle,createItem,createTxPackageInvalidDatesFAIL.xml";
		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		
		PlaceHolderMessageValidator validator = new PlaceHolderMessageValidator(unmarhsaller);
		validator.validateFile(filepath);
	
	}

}
