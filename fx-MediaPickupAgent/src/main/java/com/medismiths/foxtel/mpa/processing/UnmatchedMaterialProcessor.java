package com.medismiths.foxtel.mpa.processing;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.medismiths.foxtel.mpa.MaterialEnvelope;
import com.medismiths.foxtel.mpa.processing.MatchMaker.UnmatchedFile;

public class UnmatchedMaterialProcessor implements Runnable {

	private final Long timeout;
	private final MatchMaker matchMaker;
	private final String emergencyImportFolder;
	private boolean stopRequested = false;

	private static final long ONE_MINUTE_IN_MILLIS=1000L * 60L;
	
	private static Logger logger = Logger
			.getLogger(UnmatchedMaterialProcessor.class);

	@Inject
	public UnmatchedMaterialProcessor(
			@Named("86400000") Long timeout,
			@Named("media.path.ardomeemergencyimportfolder") String emergencyImportFolder,
			MatchMaker matchMaker) {
		this.timeout = timeout;
		this.matchMaker = matchMaker;
		this.emergencyImportFolder = emergencyImportFolder;
	}

	@Override
	public void run() {
		while (!stopRequested) {

			try {
				logger.trace("going to sleep");
				Thread.sleep(ONE_MINUTE_IN_MILLIS);
				logger.trace("woke up");
				process();

			} catch (InterruptedException e) {
				logger.warn("Interrupted", e);
				// TODO establish sensible behaviour for these sorts of
				// interrupts and apropriate sleep times
				stopRequested = true;
			}

		}
	}

	private void process() {

		processUnmatchedMessages();
		processUnmatchedMXFs();

	}

	private void processUnmatchedMXFs() {
		Collection<UnmatchedFile> unmatchedMXFs = matchMaker.purgeUnmatchedMXFs(timeout
				.longValue());
		logger.info(String.format("Found %d unmatched material mxfs",
				unmatchedMXFs.size()));

		for (UnmatchedFile mxf : unmatchedMXFs) {
			logger.info(String.format("no material message found for %s",
					mxf.getFile().getAbsolutePath()));
			/*
			 * 2.1.2.2 Media file is delivered without the companion XML file
			 * (or with a corrupt XML file) If a media file is delivered without
			 * the companion XML file (file has been not been modified in a
			 * configurable amount of time without the XML file appearing or is
			 * the XML file is not readable), the WFE shall move the media file
			 * to the emergency programme file auto import location described in
			 * section 2.3 “Emergency programme file ingest”.
			 */

			try {
				FileUtils.moveFileToDirectory(mxf.getFile(), new File(emergencyImportFolder), false);
			} catch (IOException e) {
				logger.fatal("IOException moving umatched mxf to emergency import folder",e);
			}
			
		}

	}

	private void processUnmatchedMessages() {
		Collection<MaterialEnvelope> unmatchedMessages = matchMaker
				.purgeUnmatchedMessages(timeout.longValue());
		logger.info(String.format("Found %d unmatched material messages",
				unmatchedMessages.size()));

		for (MaterialEnvelope me : unmatchedMessages) {
			logger.info(String.format("no mxf for %s", me.getFile()
					.getAbsolutePath()));
			// TODO : notify someone via email?
		}
	}

	public void stop() {
		stopRequested = true;
	}

}
