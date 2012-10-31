package com.mediasmiths.foxtel.mpa;

public final class MediaPickupConfig {

	private MediaPickupConfig(){
		//hide utility class constructor
	}
	
	public static final String ARDOME_IMPORT_FOLDER = "media.path.ardomeimportfolder";
	public static final String ARDOME_EMERGENCY_IMPORT_FOLDER = "media.path.ardomeemergencyimportfolder";
	public static final String MEDIA_COMPANION_TIMEOUT = "media.companion.timeout";
	public static final String UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES = "media.unmatched.timebetweenpurges";
	public static final String DELIVERY_ATTEMPT_COUNT = "media.delivery.attempt.count";
	public static final String IS_AO_AGENT = "media.pick.ao";

}
