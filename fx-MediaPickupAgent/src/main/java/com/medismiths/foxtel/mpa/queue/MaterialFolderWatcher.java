package com.medismiths.foxtel.mpa.queue;

import java.io.File;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;


public class MaterialFolderWatcher extends DirectoryWatchingQueuer {

	private static Logger logger = Logger.getLogger(MaterialFolderWatcher.class);
	
	@Inject
	public MaterialFolderWatcher(
			FilesPendingProcessingQueue filePathsPendingValidation, @Named("agent.path.message") String path) {
		super(filePathsPendingValidation, path);
	}
	
	@Override
	public void newFileCheck(String filePath, String fileName) {
		File file = new File(filePath);
		
		logger.debug(String.format("A file %s has arrived with path %s", fileName, filePath));
		
		if (fileName.toLowerCase(Locale.ENGLISH).endsWith(".xml")) {
			logger.info("An xml file has arrived");
			queueFile(file);
		}
		
		if (fileName.toLowerCase(Locale.ENGLISH).endsWith(".mxf")) {
			logger.info("An mxf file has arrived");
			queueFile(file);
		}
	}
	
	@Override
	protected IOFileFilter getExistingFilesFilter(){
		return acceptXMLorMXFFilesFilter;
	}
	
	private static IOFileFilter acceptXMLorMXFFilesFilter = new IOFileFilter() {
		
		@Override
		public boolean accept(File dir, String name) {
			return FilenameUtils.getExtension(name).toLowerCase(Locale.ENGLISH).equals("xml");
		}
		
		@Override
		public boolean accept(File file) {
			return FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ENGLISH).equals("xml");
		}
	};

}
