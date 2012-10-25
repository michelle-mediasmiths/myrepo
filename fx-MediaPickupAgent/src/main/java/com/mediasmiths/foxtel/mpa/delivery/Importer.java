package com.mediasmiths.foxtel.mpa.delivery;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_IMPORT_FOLDER;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_ATTEMPT_COUNT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_FAILURE_ALERT_RECIPIENT;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.mayam.AlertInterface;

public class Importer implements Runnable {

	private static Logger logger = Logger.getLogger(Importer.class);

	private final PendingImportQueue pendingImports;

	private final String targetFolder;
	private final String quarrentineFolder;
	private final String archiveFolder;
	private final int deliveryAttemptsToMake;

	private final AlertInterface alert;
	private final String deliveryFailureAlertReceipient;

	@Inject
	public Importer(
			PendingImportQueue pendingImports,
			@Named(ARDOME_IMPORT_FOLDER) String targetFolder,
			@Named(FAILURE_PATH) String quarrentineFolder,
			@Named(ARCHIVE_PATH) String archiveFolder,
			@Named(DELIVERY_ATTEMPT_COUNT) String deliveryAttemptsToMake,
			@Named(DELIVERY_FAILURE_ALERT_RECIPIENT) String deliveryFailureAlertReceipient,
			AlertInterface alert) {
		this.pendingImports = pendingImports;
		this.targetFolder = targetFolder;
		this.quarrentineFolder = quarrentineFolder;
		this.archiveFolder = archiveFolder;
		this.deliveryAttemptsToMake = Integer.parseInt(deliveryAttemptsToMake);
		this.alert = alert;
		this.deliveryFailureAlertReceipient = deliveryFailureAlertReceipient;
	}

	@Override
	public void run() {
		logger.debug("Importer start");

		while (!Thread.interrupted()) {
			try {
				PendingImport pi = pendingImports.take();
				logger.info("Picked up an import");
				deliver(pi, 1);
				logger.trace("Finished with import");
			} catch (InterruptedException e) {
				logger.info("Interruped!", e);
				return;
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
	protected void deliver(PendingImport pi, int attempt) {

		if (pi.getMaterialEnvelope().getMasterID() == null
				|| pi.getMaterialEnvelope().getMasterID().equals("null")) {
			logger.error("Missing masterID in PendingImport");
		}

		File src = pi.getMediaFile();
		File dst = new File(targetFolder, pi.getMaterialEnvelope()
				.getMasterID() + ".mxf");

		logger.debug(String.format(
				"Attempting to move from %s to %s (attempt %d)",
				src.getAbsolutePath(), dst.getAbsolutePath(), attempt));

		try {
			FileUtils.moveFile(src, dst);
		} catch (IOException e) {
			logger.error(String.format(
					"Error moving file from %s to %s on attempt number %d",
					src.getAbsolutePath(), dst.getAbsolutePath(), attempt), e);

			// allows a configurable number of retries
			if (attempt == deliveryAttemptsToMake) {
				onDeliveryFailure(pi);
			} else {
				deliver(pi, attempt + 1);
			}

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
			logger.error(
					String.format(
							"Error moving file from %s to %s though move for media succeeded",
							src.getAbsolutePath(), dst.getAbsolutePath()), e);

			// send out alert that companion xml did not move to archive
			StringBuilder sb = new StringBuilder();
			sb.append(String
					.format("There has been a failure to archive companion xml for material %s though the material successfully moved to the Viz Ardome auto import location",
							pi.getMaterialEnvelope().getMasterID()));
			alert.sendAlert(deliveryFailureAlertReceipient,
					"Media Pickup Companion XML archive failure", sb.toString());

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
						src, dst), e);
			}

			src = pi.getMaterialEnvelope().getFile();
			dst = new File(quarrentineFolder, pi.getMaterialEnvelope()
					.getMasterID() + FilenameUtils.EXTENSION_SEPARATOR + "xml");

			try {
				FileUtils.moveFile(src, dst);
			} catch (IOException e) {
				logger.fatal(String.format("Error moving file from %s to %s",
						src, dst), e);
			}
		} finally {

			// send out alert for failed import
			StringBuilder sb = new StringBuilder();
			sb.append(String
					.format("There has been a failure to deliver material to the Viz Ardome auto import location"));
			sb.append(String.format(
					"The material with ID %s has been quarrentined", pi
							.getMaterialEnvelope().getMasterID()));
			alert.sendAlert(deliveryFailureAlertReceipient,
					"Media Pickup Failure", sb.toString());

		}

	}

	protected PendingImportQueue getPendingImports() {
		return pendingImports;
	}
}
