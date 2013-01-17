package com.mediasmiths.foxtel.mpa.delivery;

import static com.mediasmiths.foxtel.agent.Config.WATCHFOLDER_LOCATIONS;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_ATTEMPT_COUNT;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;

public class DoNothingImporter extends Importer {

	@Inject
	public DoNothingImporter(
			PendingImportQueue pendingImports,
			@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders,
			@Named(DELIVERY_ATTEMPT_COUNT) String deliveryAttemptsToMake,
			EventService event) {
		super(pendingImports, watchFolders,
				deliveryAttemptsToMake, event);
	}

	@Override
	public void run() {

	}

}
