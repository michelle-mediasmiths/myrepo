package com.mediasmiths.foxtel.mpa.processing;

import static com.mediasmiths.foxtel.agent.Config.WATCHFOLDER_LOCATIONS;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.ip.event.EventService;

public class DoNothingUnmatchedMaterial extends UnmatchedMaterialProcessor {

	@Inject
	public DoNothingUnmatchedMaterial(
			@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders, EventService event) {
		super(watchFolders, event);
	}


}
