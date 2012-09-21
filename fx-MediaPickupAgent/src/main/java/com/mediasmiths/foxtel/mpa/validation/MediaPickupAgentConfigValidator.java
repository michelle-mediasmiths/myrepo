package com.mediasmiths.foxtel.mpa.validation;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.agent.Config.MESSAGE_PATH;
import static com.mediasmiths.foxtel.agent.Config.RECEIPT_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_EMERGENCY_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MEDIA_COMPANION_TIMEOUT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.validation.ConfigValidationFailureException;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;

public class MediaPickupAgentConfigValidator extends ConfigValidator {


	@Inject
	public MediaPickupAgentConfigValidator(
			@Named(MESSAGE_PATH) String messagePath,
			@Named(FAILURE_PATH) String failurePath,
			@Named(ARCHIVE_PATH) String archivePath,
			@Named(RECEIPT_PATH) String receiptPath,
			@Named(ARDOME_IMPORT_FOLDER) String importFolder,
			@Named(ARDOME_EMERGENCY_IMPORT_FOLDER) String emergencyImportFolder,
			@Named(MEDIA_COMPANION_TIMEOUT) String companionTimeout,
			@Named(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES) String timeBetweenPurges)
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

		if (anyFailures) {
			onFailure();
		}
	}

	private boolean isValidLong(String value) {
		try {
			Long l = Long.parseLong(value); //sonar complains about this but I dont care, its the handiest way to make this check
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}

	}

}
