package com.medismiths.foxtel.mpa.validation;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.validation.ConfigValidationFailureException;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;

public class MediaPickupAgentConfigValidator extends ConfigValidator {

	protected static final String ARDOME_IMPORT_FOLDER = "media.path.ardomeimportfolder";
	
	@Inject
	public MediaPickupAgentConfigValidator(@Named(MESSAGE_PATH) String messagePath,
			   @Named(FAILURE_PATH) String failurePath,
			   @Named(ARCHIVE_PATH) String archivePath,
			   @Named(RECEIPT_PATH) String receiptPath,
			   @Named(ARDOME_IMPORT_FOLDER) String importFolder)
			throws ConfigValidationFailureException {
		super(messagePath, failurePath, archivePath, receiptPath);
		
		boolean anyFailures = false;
		
		if(haveReadWritePermissions(ARDOME_IMPORT_FOLDER)){
			configValidationPasses(MESSAGE_PATH, messagePath);
		}
		else{
			anyFailures = true;
			configValidationFails(ARDOME_IMPORT_FOLDER, importFolder);
		}
		
		if(anyFailures){
			onFailure();
		}
	}

}
