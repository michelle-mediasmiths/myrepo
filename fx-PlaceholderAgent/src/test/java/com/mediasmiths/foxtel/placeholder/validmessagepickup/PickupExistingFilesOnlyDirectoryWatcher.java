package com.mediasmiths.foxtel.placeholder.validmessagepickup;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.queue.FilePickUpProcessingQueue;

public class PickupExistingFilesOnlyDirectoryWatcher extends
		DirectoryWatchingQueuer {

	private static Logger logger = Logger.getLogger(PickupExistingFilesOnlyDirectoryWatcher.class);
	
	@Inject
	public PickupExistingFilesOnlyDirectoryWatcher(
			FilePickUpProcessingQueue filePathsPendingValidation,	@Named("watchfolder.locations") WatchFolders paths) {
		super(filePathsPendingValidation, paths);
	} 

	
	@Override
	public void run() {
		
		logger.trace("PickupExistingFilesOnlyDirectoryWatcher.run() enter");
		
		//queue existing files for processing only, dont do any monitoring
		queueExistingFiles();
		
		
		logger.trace("PickupExistingFilesOnlyDirectoryWatcher.run() exit");
	}

	@Override
	protected void queueExistingFiles() {

		for (String path : sourcePaths) {
			logger.info("Checking for existing files in " + path);

			// first of all look for existing xml files before we start
			// monitoring for new ones
			Collection<File> existingFiles = listFiles(path);

			// sort by date so oldest is processed first
			List<File> existingFilesList = sortFilesByDate(existingFiles);

			for (File f : existingFilesList) {
				logger.info("Queuing existing file: " + f.getAbsolutePath());
				
				Path p = FileSystems.getDefault().getPath(f.getAbsolutePath());
				
				if (!Files.isDirectory(p, NOFOLLOW_LINKS)) {
					queueFile(f);
				}			
			}
		}
	}

}
