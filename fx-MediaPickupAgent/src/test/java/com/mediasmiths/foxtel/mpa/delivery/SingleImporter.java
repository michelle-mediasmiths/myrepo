package com.mediasmiths.foxtel.mpa.delivery;

import static com.mediasmiths.foxtel.agent.Config.WATCHFOLDER_LOCATIONS;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_ATTEMPT_COUNT;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;

public class SingleImporter extends Importer {

	@Inject
	public SingleImporter(PendingImportQueue pendingImports,
			@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders,
			@Named(DELIVERY_ATTEMPT_COUNT) String deliveryAttemptsToMake,
			EventService event) {
		super(pendingImports, watchFolders, deliveryAttemptsToMake, event);
	}

	private static Logger logger = Logger.getLogger(SingleImporter.class);

	@Override
	public void run() {
		logger.debug("Importer start");

		try {
			PendingImport pi = getPendingImports().take();
			logger.info("Picked up an import");
			deliver(pi, 1);
			logger.trace("Finished with import");
		} catch (InterruptedException e) {
			logger.info("Interruped!", e);
			return;
		}

		logger.debug("Importer stop");
	}

}
