package com.medismiths.foxtel.mpa.validation;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.validation.ConfigValidationFailureException;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;

public class MediaPickupAgentConfigValidator extends ConfigValidator {

	protected static final String ARDOME_IMPORT_FOLDER = "media.path.ardomeimportfolder";
	protected static final String ARDOME_EMERGENCY_IMPORT_FOLDER = "media.path.ardomeemergencyimportfolder";
	protected static final String MEDIA_DIGEST_ALGORITH = "media.digest.algorithm";
	protected static final String MEDIA_COMPANION_TIMEOUT = "media.companion.timeout";
	protected static final String UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES = "media.unmatched.timebetweenpurges";
	
	private static Logger logger = Logger.getLogger(MediaPickupAgentConfigValidator.class);
	
	@Inject
	public MediaPickupAgentConfigValidator(@Named(MESSAGE_PATH) String messagePath,
			   @Named(FAILURE_PATH) String failurePath,
			   @Named(ARCHIVE_PATH) String archivePath,
			   @Named(RECEIPT_PATH) String receiptPath,
			   @Named(ARDOME_IMPORT_FOLDER) String importFolder,
			   @Named(ARDOME_EMERGENCY_IMPORT_FOLDER) String emergencyImportFolder,
			   @Named(MEDIA_DIGEST_ALGORITH)String digestAlgorithm,
			   @Named(MEDIA_COMPANION_TIMEOUT) String companionTimeout,
			   @Named(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES) String timeBetweenPurges)
			throws ConfigValidationFailureException {
		super(messagePath, failurePath, archivePath, receiptPath);
		
		boolean anyFailures = false;
		
		if(haveReadWritePermissions(importFolder)){
			configValidationPasses(ARDOME_IMPORT_FOLDER, importFolder);
		}
		else{
			anyFailures = true;
			configValidationFails(ARDOME_IMPORT_FOLDER, importFolder);
		}
		
		if(haveReadWritePermissions(emergencyImportFolder)){
			configValidationPasses(ARDOME_EMERGENCY_IMPORT_FOLDER, emergencyImportFolder);
		}
		else{
			anyFailures = true;
			configValidationFails(ARDOME_EMERGENCY_IMPORT_FOLDER, emergencyImportFolder);
		}
		
		if(isValidDigestAlgorithm(digestAlgorithm)){
			configValidationPasses(MEDIA_DIGEST_ALGORITH, digestAlgorithm);
		}else{
			anyFailures=true;
			configValidationFails(MEDIA_DIGEST_ALGORITH, digestAlgorithm);
		}
		
		if(isValidLong(companionTimeout)){
			configValidationPasses(MEDIA_COMPANION_TIMEOUT, companionTimeout);
		}
		else{
			anyFailures=true;
			configValidationFails(MEDIA_COMPANION_TIMEOUT, companionTimeout);
		}
		
		if(isValidLong(timeBetweenPurges)){
			configValidationPasses(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES,timeBetweenPurges);
		}
		else{
			anyFailures=true;
			configValidationFails(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES,timeBetweenPurges);
		}
		
		if(anyFailures){
			onFailure();
		}
	}

	private boolean isValidDigestAlgorithm(String digestAlgorithm) {
		// TODO Auto-generated method stub
		// is valid checksum algorithm a better name (digest + checksum are related but not the same thing are they?)
		// see FX-29  
		return false;
	}
	
	private boolean isValidLong(String value){
		try{
			Long l = Long.parseLong(value);
			return true;
		}
		catch(NumberFormatException nfe){
			return false;
		}
		
	}

}
