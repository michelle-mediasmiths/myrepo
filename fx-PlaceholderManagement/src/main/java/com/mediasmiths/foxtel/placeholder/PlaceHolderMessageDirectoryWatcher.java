package com.mediasmiths.foxtel.placeholder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.FileWatcher.DirectoryWatcher;

public class PlaceHolderMessageDirectoryWatcher extends DirectoryWatcher implements
		Runnable {

	private final String path;
	private final FilesPendingProcessingQueue filePathsPendingValidation;

	private static Logger logger = Logger.getLogger(PlaceHolderMessageDirectoryWatcher.class);
	
	@Inject
	public PlaceHolderMessageDirectoryWatcher(FilesPendingProcessingQueue filePathsPendingValidation, @Named("placeholder.path.message") String path) {
		this.filePathsPendingValidation = filePathsPendingValidation;
		this.path = path;
		setFormatCheck(true);
	}

	@Override
	public void newFileCheck(String filePath, String fileName) {
		File file = new File(filePath);
		
		logger.debug(String.format("A file %s has arrived with path %s", fileName, filePath));
		
		if (fileName.toLowerCase().endsWith(".xml")) {
			logger.info("An xml file has arrived");
			this.filePathsPendingValidation.add(file.getAbsolutePath());
		}
	}

	@Override
	public void run() {
		
		//first of all look for existing xml files before we start monitoring for new ones
		queueExistingFiles();
		
		try {
			this.start(path);
		} catch (IOException e) {
			PlaceHolderManager.logger.fatal("Failed to register watch service", e);
			System.exit(1);
		}
	}

	/**
	 * On startup check for any existing placeholder messages that may have arrived when the serivce was not running
	 */
	protected void queueExistingFiles() {
		
		logger.info("Checking for existing files in " + path);
		
		//first of all look for existing xml files before we start monitoring for new ones
		Collection<File> existingFiles = FileUtils.listFiles(new File(path), new IOFileFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return FilenameUtils.getExtension(name).toLowerCase().equals("xml");
			}
			
			@Override
			public boolean accept(File file) {
				return FilenameUtils.getExtension(file.getName()).toLowerCase().equals("xml");
			}
		}, TrueFileFilter.INSTANCE);
		
		
		for(File f : existingFiles){
			logger.info("Queuing existing file: "+f.getAbsolutePath());
			
			//TODO : check there is not already a receipt for this file before trying to process
			filePathsPendingValidation.add(f.getAbsolutePath());
		}
	}

}