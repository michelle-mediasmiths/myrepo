package com.mediasmiths.foxtel.placeholder.messagecreation;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.placeholder.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageDirectoryWatcher;

public class PickupExistingFilesOnlyDirectoryWatcher extends
		PlaceHolderMessageDirectoryWatcher {

	private static Logger logger = Logger.getLogger(PickupExistingFilesOnlyDirectoryWatcher.class);
	
	@Inject
	public PickupExistingFilesOnlyDirectoryWatcher(
			FilesPendingProcessingQueue filePathsPendingValidation,@Named("placeholder.path.message") String path) {
		super(filePathsPendingValidation, path);
	}

	
	@Override
	public void run() {
		
		logger.trace("PickupExistingFilesOnlyDirectoryWatcher.run() enter");
		
		//queue existing files for processing only, dont do any monitoring
		queueExistingFiles();
		
		logger.trace("PickupExistingFilesOnlyDirectoryWatcher.run() exit");
	}
}
