package com.mediasmiths.foxtel.mpa.delivery;

import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_ATTEMPT_COUNT;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;

public class DoNothingImporter extends Importer {

	@Inject
	public DoNothingImporter(
			PendingImportQueue pendingImports,
			@Named(ARDOME_IMPORT_FOLDER) String targetFolder,
			@Named(DELIVERY_ATTEMPT_COUNT) String deliveryAttemptsToMake,
			EventService event) {
		super(pendingImports, targetFolder,
				deliveryAttemptsToMake, event);
	}

	@Override
	public void run() {

	}

}
