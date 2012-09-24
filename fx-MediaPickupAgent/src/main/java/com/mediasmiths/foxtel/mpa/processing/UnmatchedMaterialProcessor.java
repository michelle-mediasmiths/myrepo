package com.mediasmiths.foxtel.mpa.processing;

import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_EMERGENCY_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MEDIA_COMPANION_TIMEOUT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.mpa.MaterialEnvelope;

public class UnmatchedMaterialProcessor implements Runnable {

	private final Long timeout;
	private final MatchMaker matchMaker;
	private final String emergencyImportFolder;
	private final String failedMessagesFolder;
	private final long sleepTime;
	private boolean stopRequested = false;


	private static Logger logger = Logger
			.getLogger(UnmatchedMaterialProcessor.class);

	@Inject
	public UnmatchedMaterialProcessor(
			@Named(MEDIA_COMPANION_TIMEOUT) Long timeout,
			@Named(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES) Long sleepTime,
			@Named(ARDOME_EMERGENCY_IMPORT_FOLDER) String emergencyImportFolder,
			@Named(FAILURE_PATH) String failedMessagesFolder,
			MatchMaker matchMaker) {
		this.timeout = timeout;
		this.matchMaker = matchMaker;
		this.emergencyImportFolder = emergencyImportFolder;
		this.failedMessagesFolder=failedMessagesFolder;
		this.sleepTime=sleepTime.longValue();
	}

	@Override
	public void run() {
		while (!stopRequested) {

			try {
				logger.trace("going to sleep");
				Thread.sleep(sleepTime);
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

	protected void process() {

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
					mxf.getFilePath()));
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
				FileUtils.moveFileToDirectory(new File(mxf.getFilePath()), new File(emergencyImportFolder), false);
			} catch (IOException e) {
				logger.fatal("IOException moving umatched mxf to emergency import folder",e);
			}
			
			// TODO : notify someone via email?
			
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
			
			//move message to failure folder
			try {
				FileUtils.moveFileToDirectory(me.getFile(), new File(failedMessagesFolder), false);
			} catch (IOException e) {
				logger.fatal("IOException moving umatched xml to the failed messages folder",e);
			}
			
			// TODO : notify someone via email?
		}
	}

	public void stop() {
		stopRequested = true;
	}

}