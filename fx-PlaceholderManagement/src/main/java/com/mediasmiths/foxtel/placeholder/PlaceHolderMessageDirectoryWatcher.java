package com.mediasmiths.foxtel.placeholder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.mediasmiths.FileWatcher.DirectoryWatcher;

class PlaceHolderMessageDirectoryWatcher extends DirectoryWatcher implements
		Runnable {

	private final String path;
	private final LinkedBlockingQueue<String> filePathsPendingValidation;

	private static Logger logger = Logger.getLogger(PlaceHolderMessageDirectoryWatcher.class);
	
	public PlaceHolderMessageDirectoryWatcher(LinkedBlockingQueue<String> filePathsPendingValidation, String path) {
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
		try {
			this.start(path);
		} catch (IOException e) {
			PlaceHolderManager.logger.fatal("Failed to register watch service", e);
			System.exit(1);
		}
	}

}