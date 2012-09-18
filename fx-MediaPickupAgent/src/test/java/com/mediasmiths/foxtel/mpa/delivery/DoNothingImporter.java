package com.mediasmiths.foxtel.mpa.delivery;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.medismiths.foxtel.mpa.delivery.Importer;
import com.medismiths.foxtel.mpa.queue.PendingImportQueue;


public class DoNothingImporter extends Importer {

	@Inject
	public DoNothingImporter(PendingImportQueue pendingImports,
			@Named("media.path.ardomeimportfolder") String targetFolder,
			@Named("agent.path.failure") String quarrentineFolder,
			@Named("agent.path.archive") String archiveFolder) {
		super(pendingImports, targetFolder, quarrentineFolder, archiveFolder);
	}

	@Override
	public void run() {
		
	}
	
}
