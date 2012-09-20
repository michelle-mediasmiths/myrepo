package com.mediasmiths.foxtel.placeholder.validmessagepickup;
import static com.mediasmiths.foxtel.agent.Config.MESSAGE_PATH;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;

public class PickupExistingFilesOnlyDirectoryWatcher extends
		DirectoryWatchingQueuer {

	private static Logger logger = Logger.getLogger(PickupExistingFilesOnlyDirectoryWatcher.class);
	
	@Inject
	public PickupExistingFilesOnlyDirectoryWatcher(
			FilesPendingProcessingQueue filePathsPendingValidation,@Named(MESSAGE_PATH) String path) {
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
