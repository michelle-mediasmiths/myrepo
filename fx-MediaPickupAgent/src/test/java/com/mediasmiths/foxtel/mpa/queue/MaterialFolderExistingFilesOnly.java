/**package com.mediasmiths.foxtel.mpa.queue;


import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.queue.FilePickUpProcessingQueue;

public class MaterialFolderExistingFilesOnly extends MaterialFolderWatcher {

	private static Logger logger = Logger.getLogger(MaterialFolderExistingFilesOnly.class);
	
	@Inject
	public MaterialFolderExistingFilesOnly(
			FilePickUpProcessingQueue filePathsPendingValidation, RuzzFilesPendingProcessingQueue ruzzPathsPendingValidation, @Named("watchfolder.locations") WatchFolders paths) {
		super(filePathsPendingValidation,ruzzPathsPendingValidation, paths,100l, 5l);
	}

	@Override
	public void run() {
		
		logger.trace("MaterialFolderExistingFilesOnly.run() enter");
		
		//queue existing files for processing only, dont do any monitoring
		queueExistingFiles();
		
		logger.trace("MaterialFolderExistingFilesOnly.run() exit");
	}
	
}
*/