package com.mediasmiths.foxtel.mpa.processing;

import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.medismiths.foxtel.mpa.MediaPickupConfig.ARDOME_EMERGENCY_IMPORT_FOLDER;
import static com.medismiths.foxtel.mpa.MediaPickupConfig.MEDIA_COMPANION_TIMEOUT;
import static com.medismiths.foxtel.mpa.MediaPickupConfig.UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.medismiths.foxtel.mpa.processing.MatchMaker;
import com.medismiths.foxtel.mpa.processing.UnmatchedMaterialProcessor;

public class DoNothingUnmatchedMaterial extends UnmatchedMaterialProcessor {

	@Inject
	public DoNothingUnmatchedMaterial(
			@Named(MEDIA_COMPANION_TIMEOUT) Long timeout,
			@Named(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES) Long sleepTime,
			@Named(ARDOME_EMERGENCY_IMPORT_FOLDER) String emergencyImportFolder,
			@Named(FAILURE_PATH) String failedMessagesFolder,
			MatchMaker matchMaker) {
		super(timeout, sleepTime, emergencyImportFolder, failedMessagesFolder,
				matchMaker);
	}

	@Override
	public void run() {

	}

}
