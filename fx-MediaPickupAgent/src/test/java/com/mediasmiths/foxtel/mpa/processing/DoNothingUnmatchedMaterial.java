package com.mediasmiths.foxtel.mpa.processing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.medismiths.foxtel.mpa.processing.MatchMaker;
import com.medismiths.foxtel.mpa.processing.UnmatchedMaterialProcessor;


public class DoNothingUnmatchedMaterial extends UnmatchedMaterialProcessor {

	@Inject
	public DoNothingUnmatchedMaterial(@Named("media.companion.timeout") Long timeout,
			@Named("media.path.ardomeemergencyimportfolder") String emergencyImportFolder,
			MatchMaker matchMaker) {
		super(timeout, emergencyImportFolder, matchMaker);
	}
	
	@Override
	public void run() {
		
	}

}
