package com.mediasmiths.foxtel.mpa.queue;

import static com.mediasmiths.foxtel.agent.Config.MESSAGE_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MXF_NOT_TOUCHED_PERIOD;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.XML_NOT_TOUCHED_PERIOD;

import java.io.File;
import java.nio.file.Path;
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
			FilesPendingProcessingQueue filePathsPendingValidation, @Named(MESSAGE_PATH) String path,
			@Named(MXF_NOT_TOUCHED_PERIOD) Long mxfNotTouchedPeriod,
			@Named(XML_NOT_TOUCHED_PERIOD) Long xmlNotTouchedPeriod) {
		super(filePathsPendingValidation, path);
		
		setNotTouchedPeriodForFormat("mxf", mxfNotTouchedPeriod);
		setNotTouchedPeriodForFormat("xml", xmlNotTouchedPeriod);
	}
	
	@Override
	public void onFileArrival(Path path) {
		File file = path.toFile();
		 
		logger.debug(String.format("A file %s has arrived",file.getAbsolutePath()));
		
		if (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".xml")) {
			logger.info("An xml file has arrived");
			queueFile(file);
		}
		
		if (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".mxf")) {
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
			return accept(name);
		}
		
		@Override
		public boolean accept(File file) {
			return accept(file.getName());
		}
		
		protected boolean accept(String name){
			String extension = FilenameUtils.getExtension(name).toLowerCase(Locale.ENGLISH);	
			return extension.equals("xml") || extension.equals("mxf");
		}
		
	};

}
