package com.mediasmiths.foxtel.placeholder.validmessagepickup;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolder;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;

public class PickupExistingFilesOnlyDirectoryWatcher extends
		DirectoryWatchingQueuer {

	private static Logger logger = Logger.getLogger(PickupExistingFilesOnlyDirectoryWatcher.class);
	
	@Inject
	public PickupExistingFilesOnlyDirectoryWatcher(
			FilesPendingProcessingQueue filePathsPendingValidation,	@Named("watchfolder.locations") WatchFolders paths) {
		super(filePathsPendingValidation, paths);
	} 

	
	@Override
	public void run() {
		
		logger.trace("PickupExistingFilesOnlyDirectoryWatcher.run() enter");
		
		//queue existing files for processing only, dont do any monitoring
		queueExistingFiles();
		
		logger.trace("PickupExistingFilesOnlyDirectoryWatcher.run() exit");
	}
}
