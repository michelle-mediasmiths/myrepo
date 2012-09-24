package com.mediasmiths.foxtel.mpa.delivery;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_IMPORT_FOLDER;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.delivery.Importer;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;

public class SingleImporter extends Importer {

	private static Logger logger = Logger.getLogger(SingleImporter.class);
	
	@Inject
	public SingleImporter(PendingImportQueue pendingImports,
			@Named(ARDOME_IMPORT_FOLDER) String targetFolder,
			@Named(FAILURE_PATH) String quarrentineFolder,
			@Named(ARCHIVE_PATH) String archiveFolder) {
		super(pendingImports, targetFolder, quarrentineFolder, archiveFolder);
	}

	@Override
	public void run() {
		logger.debug("Importer start");

		try {
			PendingImport pi = getPendingImports().take();
			logger.info("Picked up an import");
			deliver(pi);
			logger.trace("Finished with import");
		} catch (InterruptedException e) {
			logger.info("Interruped!", e);
			stop();
		}

		logger.debug("Importer stop");
	}

}
