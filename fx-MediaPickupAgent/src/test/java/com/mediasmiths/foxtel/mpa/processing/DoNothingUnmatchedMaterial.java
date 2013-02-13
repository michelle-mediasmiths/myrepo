package com.mediasmiths.foxtel.mpa.processing;

import static com.mediasmiths.foxtel.agent.Config.WATCHFOLDER_LOCATIONS;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_EMERGENCY_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MEDIA_COMPANION_TIMEOUT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.ip.event.EventService;

public class DoNothingUnmatchedMaterial extends UnmatchedMaterialProcessor {

	@Inject
	public DoNothingUnmatchedMaterial(
			@Named(MEDIA_COMPANION_TIMEOUT) Long timeout,
			@Named(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES) Long sleepTime,
			@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders,
			MatchMaker matchMaker, EventService event) {
		super(timeout, sleepTime, watchFolders, matchMaker, event);
	}

	@Override
	public void run() {

	}

}
