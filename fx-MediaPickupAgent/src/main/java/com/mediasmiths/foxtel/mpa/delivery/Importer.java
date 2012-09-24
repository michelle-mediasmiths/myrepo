package com.mediasmiths.foxtel.mpa.delivery;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_IMPORT_FOLDER;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;

public class Importer implements Runnable {

	private static Logger logger = Logger.getLogger(Importer.class);

	private final PendingImportQueue pendingImports;

	private boolean stopRequested = false;
	private final String targetFolder;
	private final String quarrentineFolder;
	private final String archiveFolder;

	@Inject
	public Importer(PendingImportQueue pendingImports,
			@Named(ARDOME_IMPORT_FOLDER) String targetFolder,
			@Named(FAILURE_PATH) String quarrentineFolder,
			@Named(ARCHIVE_PATH) String archiveFolder) {
		this.pendingImports = pendingImports;
		this.targetFolder = targetFolder;
		this.quarrentineFolder = quarrentineFolder;
		this.archiveFolder = archiveFolder;
	}

	@Override
	public void run() {
		logger.debug("Importer start");

		while (!stopRequested) {
			try {
				PendingImport pi = pendingImports.take();
				logger.info("Picked up an import");
				deliver(pi);
				logger.trace("Finished with import");
			} catch (InterruptedException e) {
				logger.info("Interruped!", e);
				stop();
			}
		}

		logger.debug("Importer stop");
	}

	/**
	 * Once the WFE has established the match between the delivered media file
	 * and an Item placeholder in Viz Ardome, the WFE will move the media file
	 * from the delivery location to a Viz Ardome auto import location. As part
	 * of the file move, the WFE will rename the media file to the “Master ID”
	 * of the Item placeholder in Viz Ardome. Once the media file has been moved
	 * to the Viz Ardome auto import location, the WFE shall move the companion
	 * XML file to a corporate share to be nominated by FOXTEL.
	 * 
	 * @param pi
	 */
	protected void deliver(PendingImport pi) {

		if(pi.getMaterialEnvelope().getMasterID() == null || pi.getMaterialEnvelope().getMasterID().equals("null")){
			logger.error("Missing masterID in PendingImport");
		}
		
		File src = pi.getMediaFile();
		File dst = new File(targetFolder, pi.getMaterialEnvelope()
				.getMasterID() + ".mxf");

		logger.debug(String.format("Attempting to move from %s to %s",
				src.getAbsolutePath(), dst.getAbsolutePath()));

		try {
			FileUtils.moveFile(src, dst);
		} catch (IOException e) {
			logger.error(String.format("Error moving file from %s to %s",
					src.getAbsolutePath(), dst.getAbsolutePath()),e);
			onDeliveryFailure(pi);

			// TODO allow a configurable number of retries

			return;
		}

		src = pi.getMaterialEnvelope().getFile();
		dst = new File(archiveFolder, pi.getMaterialEnvelope().getMasterID()
				+ ".xml");

		logger.debug(String.format("Attempting to move from %s to %s",
				src.getAbsolutePath(), dst.getAbsolutePath()));

		try {
			FileUtils.moveFile(src, dst);
		} catch (IOException e) {
			logger.error(String
					.format("Error moving file from %s to %s though move for media succeeded",
							src.getAbsolutePath(), dst.getAbsolutePath()),e);
			// TODO : notify someone
			return;
		}
	}

	/**
	 * 2.1.2.5 Moving media file to either Viz Ardome auto import location fails
	 * If the WFE fails to move the file to the auto import location after a
	 * number of retries, the WFE shall move the media file along with the
	 * companion XML file to a quarantine area to be nominated by FOXTEL and
	 * send an e-mail notification to an e-mail alias (or list) to be defined by
	 * FOXTEL
	 */
	private void onDeliveryFailure(PendingImport pi) {
		try {
			File src = pi.getMediaFile();
			File dst = new File(quarrentineFolder, pi.getMaterialEnvelope()
					.getMasterID() + FilenameUtils.EXTENSION_SEPARATOR + "mxf");

			try {
				FileUtils.moveFile(src, dst);
			} catch (IOException e) {
				logger.fatal(String.format("Error moving file from %s to %s",
						src, dst),e);
			}

			src = pi.getMaterialEnvelope().getFile();
			dst = new File(quarrentineFolder, pi.getMaterialEnvelope()
					.getMasterID() + FilenameUtils.EXTENSION_SEPARATOR +  "xml");

			try {
				FileUtils.moveFile(src, dst);
			} catch (IOException e) {
				logger.fatal(String.format("Error moving file from %s to %s",
						src, dst),e);
			}
		} finally {
			// TODO send out notification emails
		}

	}

	protected PendingImportQueue getPendingImports() {
		return pendingImports;
	}

	public void stop() {
		stopRequested = true;
	}
}
