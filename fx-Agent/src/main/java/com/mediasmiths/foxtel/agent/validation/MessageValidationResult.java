package com.mediasmiths.foxtel.agent.validation;

public enum MessageValidationResult {
	IS_VALID, 
	FAILS_XSD_CHECK, 
	FAILED_TO_UNMARSHALL, 
	UNEXPECTED_TYPE, 
	ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS, 
	//invalid message id may be returned if the receipt for a given message already exists (ie the messageid could be a duplicate)
	INVALID_MESSAGE_ID, INVALID_SENDER_ID, 
	INVALID_PRIVATE_MESSAGE_DATA, 
	NO_EXISTING_TITLE_FOR_MATERIAL,
	NO_EXISTING_MATERIAL_FOR_PACKAGE, 
	LICENCE_DATES_NOT_IN_ORDER, 
	ORDER_CREATED_AND_REQUIREDBY_DATES_NOT_IN_ORDER, 
	PACKAGES_MATERIAL_IS_PROTECTED, 
	UNKOWN_VALIDATION_FAILURE, 
	TITLE_OR_DESCENDANT_IS_PROTECTED,
	TITLE_DOES_NOT_EXIST,
	MATERIAL_DOES_NOT_EXIST,
	PACKAGE_DOES_NOT_EXIST,
	MATERIAL_IS_NOT_PLACEHOLDER,
	MAYAM_CLIENT_ERROR,
	TITLEID_IS_NULL_OR_EMPTY,
	MATERIALID_IS_NULL_OR_EMPTY,
	PACKAGEID_IS_NULL_OR_EMPTY,
	NO_EXISTING_TITLE_TO_PURGE,
	UNKOWN_CHANNEL,
	MATERIAL_IS_PROTECTED,
	CHANNEL_NAME_INVALID, 
	PACKAGE_INVALID_FORMAT, 
	PACKAGE_TARGET_DATE_LICENSE_INVALID, 
	TITLE_TARGET_DATE_LICENSE_INVALID,
	UNEXPECTED_DELIVERY_VERSION,
	MATERIAL_HAS_ALREADY_PASSED_PREVIEW;
}
