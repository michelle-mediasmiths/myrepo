package com.medismiths.foxtel.mpa.delivery;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.medismiths.foxtel.mpa.PendingImport;

public class Importer implements Runnable {

	private static Logger logger = Logger.getLogger(Importer.class);
	
	//imports that have been identified and are waiting to be processed
	private final Set<PendingImport> pendingImports = new HashSet<PendingImport>();
	//the time to sleep each iteration before checking for pending imports
	private final long sleepTime = 5000L;
	
	public Importer() {
	}

	@Override
	public void run() {

		while(true){
			
			PendingImport pi = pickupAnImport();
			
			if(pi != null){
				logger.info("Picked up an import");
				new FileDelivery().onAssetAndXMLArrival(pi.getXmlFile(),pi.getMediaFile(),pi.getProgramme());
				logger.trace("Finished with import");
			}
			
			try {
				Thread.currentThread().sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.warn("Importer thread interrupted",e);
			}
		}
		
	}

	private PendingImport pickupAnImport() {
		PendingImport pi = null;
		
		synchronized (pendingImports) {
			
			int pendingImportsSize = pendingImports.size();
			
			logger.trace("PendingImports size = "+pendingImportsSize);
			
			if(pendingImportsSize > 0){
				Iterator<PendingImport> iterator = pendingImports.iterator();
				pi = iterator.next();
				iterator.remove();				
			}
		}
		return pi;
	}
	
	/**
	 * Adds a pending import to be picked up by this imported
	 * @param pendingImport
	 */
	public void addPendingImport(PendingImport pendingImport){
		
		logger.info("Adding pending import");
		synchronized (pendingImports) {
			pendingImports.add(pendingImport);
		}
		
	}

}
