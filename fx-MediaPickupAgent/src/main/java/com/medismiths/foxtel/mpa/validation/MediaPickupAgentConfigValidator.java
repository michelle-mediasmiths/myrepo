package com.medismiths.foxtel.mpa.validation;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.agent.Config.MESSAGE_PATH;
import static com.mediasmiths.foxtel.agent.Config.RECEIPT_PATH;
import static com.medismiths.foxtel.mpa.MediaPickupConfig.ARDOME_EMERGENCY_IMPORT_FOLDER;
import static com.medismiths.foxtel.mpa.MediaPickupConfig.ARDOME_IMPORT_FOLDER;
import static com.medismiths.foxtel.mpa.MediaPickupConfig.MEDIA_COMPANION_TIMEOUT;
import static com.medismiths.foxtel.mpa.MediaPickupConfig.MEDIA_DIGEST_ALGORITHM;
import static com.medismiths.foxtel.mpa.MediaPickupConfig.UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES;


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
			@Named(MEDIA_DIGEST_ALGORITHM) String digestAlgorithm,
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

		if (isValidDigestAlgorithm(digestAlgorithm)) {
			configValidationPasses(MEDIA_DIGEST_ALGORITHM, digestAlgorithm);
		} else {
			anyFailures = true;
			configValidationFails(MEDIA_DIGEST_ALGORITHM, digestAlgorithm);
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

	private boolean isValidDigestAlgorithm(String digestAlgorithm) {
		// TODO Auto-generated method stub
		// is valid checksum algorithm a better name (digest + checksum are
		// related but not the same thing are they?)
		// see FX-29
		return false;
	}

	private boolean isValidLong(String value) {
		try {
			Long l = Long.parseLong(value); //sonar complains about this, is there a better way to test this?
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}

	}

}
