package com.medismiths.foxtel.mpa.delivery;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.medismiths.foxtel.mpa.PendingImport;
import com.medismiths.foxtel.mpa.queue.PendingImportQueue;

public class Importer implements Runnable {

	private static Logger logger = Logger.getLogger(Importer.class);

	private final PendingImportQueue pendingImports;
	private boolean stopRequested = false;
	private final String targetFolder;

	@Inject
	public Importer(PendingImportQueue pendingImports, @Named("media.path.ardomeimportfolder") String targetFolder) {
		this.pendingImports=pendingImports;
		this.targetFolder=targetFolder;
	}

	@Override
	public void run() {
		logger.debug("Importer start");
		
		while (!stopRequested) {
			try {
				PendingImport pi = pendingImports.take();	
				logger.info("Picked up an import");
				deliver(pi);
				logger.trace("Finished with import");
			} catch (InterruptedException e) {
				logger.info("Interruped!", e);
				stop();
			}
		}
		
		logger.debug("Importer stop");
	}
	
	private void deliver(PendingImport pi){
		
		File src = pi.getMediaFile();
		//TODO : get the material id from somewhere better, this assumes a programme, what about marketing material?
		File dst = new File(targetFolder, pi.getMaterial().getTitle().getProgrammeMaterial().getMaterialID()+".mxf");
		
		try {
			FileUtils.moveFile(src, dst);
		} catch (IOException e) {
			logger.error(String.format("Error moving file from %s to %s",src.getAbsolutePath(),dst.getAbsolutePath()));
			
			//TODO: handle error
		}
		
	}
	
	public void stop() {
		stopRequested = true;
	}
}
