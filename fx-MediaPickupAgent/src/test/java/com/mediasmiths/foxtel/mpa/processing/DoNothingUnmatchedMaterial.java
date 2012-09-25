package com.mediasmiths.foxtel.mpa.processing;

import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_EMERGENCY_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_FAILURE_ALERT_RECIPIENT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MEDIA_COMPANION_TIMEOUT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.mayam.AlertInterface;

public class DoNothingUnmatchedMaterial extends UnmatchedMaterialProcessor {

	@Inject
	public DoNothingUnmatchedMaterial(
			@Named(MEDIA_COMPANION_TIMEOUT) Long timeout,
			@Named(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES) Long sleepTime,
			@Named(ARDOME_EMERGENCY_IMPORT_FOLDER) String emergencyImportFolder,
			@Named(FAILURE_PATH) String failedMessagesFolder,
			@Named(DELIVERY_FAILURE_ALERT_RECIPIENT) String deliveryFailureAlertReceipient,
			MatchMaker matchMaker, AlertInterface alert) {
		super(timeout, sleepTime, emergencyImportFolder, failedMessagesFolder,
				deliveryFailureAlertReceipient, matchMaker, alert);
	}

	@Override
	public void run() {

	}

}
