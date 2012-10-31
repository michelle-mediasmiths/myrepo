package com.mediasmiths.foxtel.mpa.delivery;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_ATTEMPT_COUNT;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.mayam.AlertInterface;

public class DoNothingImporter extends Importer {

	@Inject
	public DoNothingImporter(
			PendingImportQueue pendingImports,
			@Named(ARDOME_IMPORT_FOLDER) String targetFolder,
			@Named(FAILURE_PATH) String quarrentineFolder,
			@Named(ARCHIVE_PATH) String archiveFolder,
			@Named(DELIVERY_ATTEMPT_COUNT) String deliveryAttemptsToMake,
			EventService event) {
		super(pendingImports, targetFolder, quarrentineFolder, archiveFolder,
				deliveryAttemptsToMake, event);
	}

	@Override
	public void run() {

	}

}
