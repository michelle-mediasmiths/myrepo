package com.mediasmiths.foxtel.agent.validation;

import java.io.File;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ConfigValidator {

	private static Logger logger = Logger.getLogger(ConfigValidator.class);
	
	protected static final String MESSAGE_PATH = "agent.path.message";
	protected static final String FAILURE_PATH = "agent.path.failure";
	protected static final String ARCHIVE_PATH = "agent.path.archive";
	protected static final String RECEIPT_PATH = "agent.path.receipt";
	
	@Inject
	public ConfigValidator(@Named(MESSAGE_PATH) String messagePath,
						   @Named(FAILURE_PATH) String failurePath,
						   @Named(ARCHIVE_PATH) String archivePath,
						   @Named(RECEIPT_PATH) String receiptPath) throws ConfigValidationFailureException{
		
		boolean anyFailures = false;
		
		if(haveReadWritePermissions(messagePath)){
			configValidationPasses(MESSAGE_PATH, messagePath);
		}
		else{
			anyFailures = true;
			configValidationFails(MESSAGE_PATH, messagePath);
		}
		
		if(haveReadWritePermissions(failurePath)){
			configValidationPasses(FAILURE_PATH, failurePath);
		}
		else{
			anyFailures = true;
			configValidationFails(FAILURE_PATH, failurePath);
		}
		
		if(haveReadWritePermissions(archivePath)){
			configValidationPasses(ARCHIVE_PATH, archivePath);
		}
		else{
			anyFailures = true;
			configValidationFails(ARCHIVE_PATH, archivePath);
		}
		
		if(haveReadWritePermissions(receiptPath)){
			configValidationPasses(RECEIPT_PATH, receiptPath);
		}
		else{
			anyFailures = true;
			configValidationFails(RECEIPT_PATH, receiptPath);
		}
		
		if(anyFailures){
			onFailure();
		}
	}

	protected final void onFailure() throws ConfigValidationFailureException {
		logger.fatal("There are config validation failures");
		throw new ConfigValidationFailureException(); //by throwing this exception guice will not construct a PlaceholderManager (which required a ConfigValidator)
	}
	
	protected final void configValidationPasses(String name, String value){
		logger.info(String.format("Using %s for %s VALIDATES", value,name));
	}
	
	protected final void configValidationFails(String name, String value){
		logger.error(String.format("Do not have read + write permissions on %s, check config value %s", value, name));
	}
	
	protected final boolean haveReadWritePermissions(String folderPath){
		
		File folder = new File(folderPath);		
		return (folder.canRead() && folder.canWrite());
	}
		
}
