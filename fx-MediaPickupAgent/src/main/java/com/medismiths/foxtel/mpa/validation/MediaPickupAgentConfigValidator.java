package com.medismiths.foxtel.mpa.validation;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.validation.ConfigValidationFailureException;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;

public class MediaPickupAgentConfigValidator extends ConfigValidator {

	protected static final String ARDOME_IMPORT_FOLDER = "media.path.ardomeimportfolder";
	protected static final String ARDOME_EMERGENCY_IMPORT_FOLDER = "media.path.ardomeemergencyimportfolder";
	
	@Inject
	public MediaPickupAgentConfigValidator(@Named(MESSAGE_PATH) String messagePath,
			   @Named(FAILURE_PATH) String failurePath,
			   @Named(ARCHIVE_PATH) String archivePath,
			   @Named(RECEIPT_PATH) String receiptPath,
			   @Named(ARDOME_IMPORT_FOLDER) String importFolder,
			   @Named(ARDOME_EMERGENCY_IMPORT_FOLDER) String emergencyImportFolder)
			throws ConfigValidationFailureException {
		super(messagePath, failurePath, archivePath, receiptPath);
		
		boolean anyFailures = false;
		
		if(haveReadWritePermissions(ARDOME_IMPORT_FOLDER)){
			configValidationPasses(ARDOME_IMPORT_FOLDER, importFolder);
		}
		else{
			anyFailures = true;
			configValidationFails(ARDOME_IMPORT_FOLDER, importFolder);
		}
		
		if(haveReadWritePermissions(ARDOME_EMERGENCY_IMPORT_FOLDER)){
			configValidationPasses(ARDOME_EMERGENCY_IMPORT_FOLDER, emergencyImportFolder);
		}
		else{
			anyFailures = true;
			configValidationFails(ARDOME_EMERGENCY_IMPORT_FOLDER, emergencyImportFolder);
		}
		
		if(anyFailures){
			onFailure();
		}
	}

}
