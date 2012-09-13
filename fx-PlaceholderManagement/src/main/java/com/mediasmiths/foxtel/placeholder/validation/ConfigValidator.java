package com.mediasmiths.foxtel.placeholder.validation;

import java.io.File;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ConfigValidator {

	private static Logger logger = Logger.getLogger(ConfigValidator.class);
	
	private static final String MESSAGE_PATH = "placeholder.path.message";
	private static final String FAILURE_PATH = "placeholder.path.failure";
	private static final String ARCHIVE_PATH = "placeholder.path.archive";
	private static final String RECEIPT_PATH = "placeholder.path.receipt";
	
	@Inject
	public ConfigValidator(@Named(MESSAGE_PATH) String messagePath,
						   @Named(FAILURE_PATH) String failurePath,
						   @Named(ARCHIVE_PATH) String archivePath,
						   @Named(RECEIPT_PATH) String receiptPath) throws ConfigValidationFailureException{
		
		boolean anyFailures = false;
		
		if(!haveReadWritePermissions(messagePath)){
			anyFailures = true;
			configValidationFails(MESSAGE_PATH, messagePath);
		}
		
		if(!haveReadWritePermissions(failurePath)){
			anyFailures = true;
			configValidationFails(FAILURE_PATH, failurePath);
		}
		
		if(!haveReadWritePermissions(archivePath)){
			anyFailures = true;
			configValidationFails(ARCHIVE_PATH, archivePath);
		}
		
		if(!haveReadWritePermissions(receiptPath)){
			anyFailures = true;
			configValidationFails(RECEIPT_PATH, receiptPath);
		}
		
		if(anyFailures){
			logger.fatal("There are config validation failures");
			throw new ConfigValidationFailureException(); //by throwing this exception guice will not construct a PlaceholderManager (which required a ConfigValidator)
		}
	}
	
	private void configValidationFails(String name, String value){
		logger.error(String.format("Do not have read + write permissions on %s, check config value %s", value, name));
	}
	
	private boolean haveReadWritePermissions(String folderPath){
		
		File folder = new File(folderPath);		
		return (folder.canRead() && folder.canWrite());
	}
		
}
