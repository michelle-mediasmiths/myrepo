package com.mediasmiths.foxtel.mpa.delivery;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_IMPORT_FOLDER;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.mpa.delivery.Importer;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;

public class DoNothingImporter extends Importer {

	@Inject
	public DoNothingImporter(PendingImportQueue pendingImports,
			@Named(ARDOME_IMPORT_FOLDER) String targetFolder,
			@Named(FAILURE_PATH) String quarrentineFolder,
			@Named(ARCHIVE_PATH) String archiveFolder) {
		super(pendingImports, targetFolder, quarrentineFolder, archiveFolder);
	}

	@Override
	public void run() {

	}

}
