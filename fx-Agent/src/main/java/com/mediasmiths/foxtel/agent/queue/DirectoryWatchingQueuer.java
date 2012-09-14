package com.mediasmiths.foxtel.agent.queue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.FileWatcher.DirectoryWatcher;

public class DirectoryWatchingQueuer extends DirectoryWatcher implements
		Runnable {

	private final String path;
	private final FilesPendingProcessingQueue filePathsPendingValidation;

	private static Logger logger = Logger.getLogger(DirectoryWatchingQueuer.class);
	
	@Inject
	public DirectoryWatchingQueuer(FilesPendingProcessingQueue filePathsPendingValidation, @Named("agent.path.message") String path) {
		this.filePathsPendingValidation = filePathsPendingValidation;
		this.path = path;
		setFormatCheck(true);
	}

	@Override
	public void newFileCheck(String filePath, String fileName) {
		File file = new File(filePath);
		
		logger.debug(String.format("A file %s has arrived with path %s", fileName, filePath));
		
		if (fileName.toLowerCase(Locale.ENGLISH).endsWith(".xml")) {
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
			logger.fatal("Failed to register watch service", e);
		}
	}

	/**
	 * On startup check for any existing messages that may have arrived when the serivce was not running
	 */
	protected void queueExistingFiles() {
		
		logger.info("Checking for existing files in " + path);
		
		//first of all look for existing xml files before we start monitoring for new ones
		Collection<File> existingFiles = listFiles();
				
		//sort by date so oldest is processed first
		List<File> existingFilesList = sortFilesByDate(existingFiles);
		
		for(File f : existingFilesList){
			logger.info("Queuing existing file: "+f.getAbsolutePath());
			filePathsPendingValidation.add(f.getAbsolutePath());
		}
	}

	private static Comparator<File> lastModifiedComparator =  new Comparator<File>(){

		@Override
		public int compare(File o1, File o2) {
			return Long.valueOf(o1.lastModified()).compareTo(Long.valueOf(o2.lastModified()));
		}};
	
	private List<File> sortFilesByDate(Collection<File> existingFiles) {
		List<File> existingFilesList  = new ArrayList<File>(existingFiles);		
		Collections.sort(existingFilesList,lastModifiedComparator);
		return existingFilesList;
	}

	private static IOFileFilter acceptXMLFilesFilter = new IOFileFilter() {
		
		@Override
		public boolean accept(File dir, String name) {
			return FilenameUtils.getExtension(name).toLowerCase(Locale.ENGLISH).equals("xml");
		}
		
		@Override
		public boolean accept(File file) {
			return FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ENGLISH).equals("xml");
		}
	};
	
	private Collection<File> listFiles() {
		Collection<File> existingFiles = FileUtils.listFiles(new File(path), acceptXMLFilesFilter, TrueFileFilter.INSTANCE);
		return existingFiles;
	}

}