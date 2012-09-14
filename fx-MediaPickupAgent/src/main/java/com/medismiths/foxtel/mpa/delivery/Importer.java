package com.medismiths.foxtel.mpa.delivery;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.medismiths.foxtel.mpa.PendingImport;
import com.medismiths.foxtel.mpa.queue.PendingImportQueue;

public class Importer implements Runnable {

	private static Logger logger = Logger.getLogger(Importer.class);

	private final PendingImportQueue pendingImports;
	private boolean stopRequested = false;

	@Inject
	public Importer(PendingImportQueue pendingImports) {
		this.pendingImports=pendingImports;
	}

	@Override
	public void run() {
		logger.debug("Importer start");
		
		while (!stopRequested) {
			try {
				PendingImport pi = pendingImports.take();	
				logger.info("Picked up an import");
				new FileDelivery().onAssetAndXMLArrival(pi.getXmlFile(),pi.getMediaFile(),pi.getMaterial());
				logger.trace("Finished with import");
			} catch (InterruptedException e) {
				logger.info("Interruped!", e);
				stop();
			}
		}
		
		logger.debug("Importer stop");
	}
	
	public void stop() {
		stopRequested = true;
	}
}
