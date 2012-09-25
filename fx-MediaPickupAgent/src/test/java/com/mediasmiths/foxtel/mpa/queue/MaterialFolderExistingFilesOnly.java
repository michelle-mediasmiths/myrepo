package com.mediasmiths.foxtel.mpa.queue;

import static com.mediasmiths.foxtel.agent.Config.MESSAGE_PATH;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;

public class MaterialFolderExistingFilesOnly extends MaterialFolderWatcher {

	private static Logger logger = Logger.getLogger(MaterialFolderExistingFilesOnly.class);
	
	@Inject
	public MaterialFolderExistingFilesOnly(
			FilesPendingProcessingQueue filePathsPendingValidation, @Named(MESSAGE_PATH)  String path) {
		super(filePathsPendingValidation, path);
	}

	@Override
	public void run() {
		
		logger.trace("MaterialFolderExistingFilesOnly.run() enter");
		
		//queue existing files for processing only, dont do any monitoring
		queueExistingFiles();
		
		logger.trace("MaterialFolderExistingFilesOnly.run() exit");
	}
	
}
