package com.mediasmiths.foxtel.placeholder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import com.mediasmiths.FileWatcher.DirectoryWatcher;

class PlaceHolderMessageDirectoryWatcher extends DirectoryWatcher implements
		Runnable {

	private final String path;
	private final LinkedBlockingQueue<String> filePathsPendingValidation;

	public PlaceHolderMessageDirectoryWatcher(LinkedBlockingQueue<String> filePathsPendingValidation, String path) {
		this.filePathsPendingValidation = filePathsPendingValidation;
		this.path = path;
	}

	@Override
	public void newFileCheck(String filePath, String fileName) {
		File file = new File(filePath + fileName);

		if (fileName.toLowerCase().endsWith(".xml")) {
			PlaceHolderManager.logger.info("An xml file has arrived");
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