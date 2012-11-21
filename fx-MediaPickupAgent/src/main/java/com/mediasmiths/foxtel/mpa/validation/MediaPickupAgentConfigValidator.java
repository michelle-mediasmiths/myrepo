package com.mediasmiths.foxtel.mpa.validation;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.agent.Config.MESSAGE_PATH;
import static com.mediasmiths.foxtel.agent.Config.RECEIPT_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_EMERGENCY_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_ATTEMPT_COUNT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.IS_AO_AGENT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MEDIA_COMPANION_TIMEOUT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MXF_NOT_TOUCHED_PERIOD;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.XML_NOT_TOUCHED_PERIOD;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.validation.ConfigValidationFailureException;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;

public class MediaPickupAgentConfigValidator extends ConfigValidator {

	private static Logger logger = Logger
			.getLogger(MediaPickupAgentConfigValidator.class);

	
	@Inject
	public MediaPickupAgentConfigValidator(
			@Named(MESSAGE_PATH) String messagePath,
			@Named(FAILURE_PATH) String failurePath,
			@Named(ARCHIVE_PATH) String archivePath,
			@Named(RECEIPT_PATH) String receiptPath,
			@Named(ARDOME_IMPORT_FOLDER) String importFolder,
			@Named(ARDOME_EMERGENCY_IMPORT_FOLDER) String emergencyImportFolder,
			@Named(MEDIA_COMPANION_TIMEOUT) String companionTimeout,
			@Named(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES) String timeBetweenPurges,
			@Named(DELIVERY_ATTEMPT_COUNT) String deliveryAttemptCount,
			@Named(IS_AO_AGENT) Boolean isAOAgent,
			@Named(MXF_NOT_TOUCHED_PERIOD) String mxfNotTouched,
			@Named(XML_NOT_TOUCHED_PERIOD) String xmlNotTouched
			)
			throws ConfigValidationFailureException {
		super(messagePath, failurePath, archivePath, receiptPath);

		boolean anyFailures = false;

		if (haveReadWritePermissions(importFolder)) {
			configValidationPasses(ARDOME_IMPORT_FOLDER, importFolder);
		} else {
			anyFailures = true;
			configValidationFails(ARDOME_IMPORT_FOLDER, importFolder);
		}

		if (haveReadWritePermissions(emergencyImportFolder)) {
			configValidationPasses(ARDOME_EMERGENCY_IMPORT_FOLDER,
					emergencyImportFolder);
		} else {
			anyFailures = true;
			configValidationFails(ARDOME_EMERGENCY_IMPORT_FOLDER,
					emergencyImportFolder);
		}

		if (isValidLong(companionTimeout)) {
			configValidationPasses(MEDIA_COMPANION_TIMEOUT, companionTimeout);
		} else {
			anyFailures = true;
			configValidationFails(MEDIA_COMPANION_TIMEOUT, companionTimeout);
		}

		if (isValidLong(timeBetweenPurges)) {
			configValidationPasses(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES,
					timeBetweenPurges);
		} else {
			anyFailures = true;
			configValidationFails(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES,
					timeBetweenPurges);
		}
		
		if (isValidLong(timeBetweenPurges)) {
			configValidationPasses(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES,
					timeBetweenPurges);
		} else {
			anyFailures = true;
			configValidationFails(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES,
					timeBetweenPurges);
		}
		
		
		if (isValidLong(xmlNotTouched)) {
			configValidationPasses(XML_NOT_TOUCHED_PERIOD,
					xmlNotTouched);
		} else {
			anyFailures = true;
			configValidationFails(XML_NOT_TOUCHED_PERIOD,
					xmlNotTouched);
		}
		
		
		if(isValidInt(mxfNotTouched)){
			configValidationPasses(MXF_NOT_TOUCHED_PERIOD,
					mxfNotTouched);
		} else {
			anyFailures = true;
			configValidationFails(MXF_NOT_TOUCHED_PERIOD,
					mxfNotTouched);
		}
		
		if(isAOAgent.booleanValue()==true){
			if(! failurePath.equals(emergencyImportFolder)){
				anyFailures=true;
				logger.error("AO agent should quarrentine unmatched content, set emergency import folder to be equal to the quarrentine / failure folder");				
				configValidationFails(ARDOME_EMERGENCY_IMPORT_FOLDER,
						emergencyImportFolder);
			}
		}

		if (anyFailures) {
			onFailure();
		}
	}

	private boolean isValidLong(String value) {
		try {
			Long l = Long.parseLong(value); // NOSONAR
			// sonar complains about this but I don't care, there is not really
			// a better way of making this check
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}

	}

	private boolean isValidInt(String value) {
		try {
			int i = Integer.parseInt(value); // NOSONAR
			return true;
		} catch (NumberFormatException nfe) {
			return false;

		}
	}

}
