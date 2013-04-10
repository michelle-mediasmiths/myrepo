package com.mediasmiths.foxtel.mpa.delivery;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord;
import com.mediasmiths.foxtel.ip.common.events.MediaPickupNotification;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;
import com.mediasmiths.std.threading.Daemon;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static com.mediasmiths.foxtel.agent.Config.WATCHFOLDER_LOCATIONS;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_ATTEMPT_COUNT;

public class Importer extends Daemon implements StoppableService {

	private static Logger logger = Logger.getLogger(Importer.class);

	private final PendingImportQueue pendingImports;

	// private final String targetFolder;
	private final WatchFolders watchFolders; // contains source to destination
												// mappings for delivery
	private final int deliveryAttemptsToMake;

	private final EventService eventService;

	public static final JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.events.report");

	@Inject
	public Importer(PendingImportQueue pendingImports,
			@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders,
			@Named(DELIVERY_ATTEMPT_COUNT) String deliveryAttemptsToMake,
			com.mediasmiths.foxtel.ip.event.EventService eventService) {
		this.pendingImports = pendingImports;
		this.watchFolders = watchFolders;
		this.deliveryAttemptsToMake = Integer.parseInt(deliveryAttemptsToMake);
		this.eventService = eventService;
	}

	@Override
	public void run() {
		logger.debug("Importer start");

		while (isRunning()) {
			try {
				PendingImport pi = pendingImports.take();
				logger.info("Picked up an import");

				try {
					deliver(pi, 1);
				} catch (Exception e) {
					logger.error(String.format(
							"Error delivering pending file %s", pi.getMaterialEnvelope().getPickupPackage().getRootName()), e);
				}

				logger.info("Finished with import");
			} catch (InterruptedException e) {
				logger.info("Interruped!", e);
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

		File src = pi.getMaterialEnvelope().getPickupPackage().getPickUp("mxf");

		final long fileSize = src.length();
		
		String targetFolder = watchFolders.destinationFor(FilenameUtils.getFullPathNoEndSeparator(src.getAbsolutePath()));
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

		src = pi.getMaterialEnvelope().getPickupPackage().getPickUp("xml");
		dst = new File(MessageProcessor.getArchivePathForFile(src
				.getAbsolutePath()), pi.getMaterialEnvelope().getMasterID()
				+ ".xml");
		
		if(dst.exists()){
			int i=1;
			
			while(dst.exists()){
				dst = new File(MessageProcessor.getArchivePathForFile(src
						.getAbsolutePath()), pi.getMaterialEnvelope().getMasterID()
						+ "_"+ i +".xml");
				i++;
			}
		}

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
			eventService.saveEvent("error", sb.toString());

			return;
		}
		saveEvent(pi,fileSize);
	}


	private void saveEvent(PendingImport pi, long fileSize)
	{
		try
		{
			Acquisition payload = new Acquisition();

			@SuppressWarnings("rawtypes")
			MediaEnvelope materialEnvelope = pi.getMaterialEnvelope();
			Object message = materialEnvelope.getMessage();

			if(message instanceof Material){

				Material m = (Material) message;

				if(com.mediasmiths.foxtel.mpa.Util.isProgramme(m))
					{
						ProgrammeMaterialType p = m.getTitle().getProgrammeMaterial();

						payload.setMaterialID(p.getMaterialID());
						payload.setTitle(m.getTitle().getEpisodeTitle());
						payload.setAggregatorID(m.getDetails().getSupplier().getSupplierID());
						payload.setFormat(p.getFormat());
						payload.setFileDelivery(true);
						payload.setTapeDelivery(false);
						payload.setFilesize(fileSize+"");
						payload.setTitleLength(p.getDuration());

						eventService.saveEvent("ProgrammeContentAvailable", JAXB_SERIALISER.serialise(payload));
					}
					else
					{
						MarketingMaterialType marketingMaterial = m.getTitle().getMarketingMaterial();

						payload.setMaterialID(materialEnvelope.getMasterID());
						payload.setTitle(m.getTitle().getEpisodeTitle());
						payload.setAggregatorID(m.getDetails().getSupplier().getSupplierID());
						payload.setFormat(marketingMaterial.getFormat());
						payload.setTapeDelivery(false);
						payload.setFileDelivery(true);
						payload.setFilesize(fileSize+"");
						payload.setTitleLength(marketingMaterial.getDuration());


						eventService.saveEvent("MarketingContentAvailable", JAXB_SERIALISER.serialise(payload));

					}

			}
			else if(message instanceof RuzzIngestRecord)
			{
				RuzzIngestRecord r = (RuzzIngestRecord)message;

				payload.setMaterialID(r.getMaterial().getMaterialID());
				payload.setTitle(r.getMaterial().getDetails().getTitle());
				payload.setAggregatorID("");
				payload.setFormat(r.getMaterial().getDetails().getFormat());
				payload.setTapeDelivery(false);
				payload.setFileDelivery(true);
				payload.setFilesize(fileSize+"");
				payload.setTitleLength(r.getMaterial().getDetails().getDuration());

				eventService.saveEvent("ProgrammeContentAvailable", JAXB_SERIALISER.serialise(payload));

			}
			else
			{
				logger.info("Unknown type being processed by importer - no event created.");
			}
		}
		catch (Throwable e)
		{
			logger.info("Unknown event processing error.", e);
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
			File src = pi.getMaterialEnvelope().getPickupPackage().getPickUp("mxf");
			String quarrentineFolder = MessageProcessor
					.getFailureFolderForFile(src);
			File baseDestination = new File(quarrentineFolder, pi
					.getMaterialEnvelope().getMasterID()
					+ FilenameUtils.EXTENSION_SEPARATOR + "mxf");
			File dst = new File(MessageProcessor.getDestinationPathForFileMove(
					baseDestination, quarrentineFolder, true));

			try {
				FileUtils.moveFile(src, dst);
			} catch (IOException e) {
				logger.fatal(String.format("Error moving file from %s to %s",
						src, dst), e);
			}

			src = pi.getMaterialEnvelope().getPickupPackage().getPickUp("xml");
			dst = new File(quarrentineFolder, pi.getMaterialEnvelope()
					.getMasterID() + FilenameUtils.EXTENSION_SEPARATOR + "xml");

			try {
				FileUtils.moveFile(src, dst);
			} catch (IOException e) {
				logger.fatal(String.format("Error moving file from %s to %s",
						src, dst), e);
			}

			try
			{
				MediaPickupNotification n = new MediaPickupNotification();
				n.setFilelocation(dst.getAbsolutePath());
				n.setTime((new Date()).toString());
				eventService.saveEvent("http://www.foxtel.com.au/ip/content", "Quarantine", n);
			}
			catch (Exception e)
			{
                logger.error("Unable to send Quarantine event ", e);
			}
		} finally {

			// send out alert for failed import
			StringBuilder sb = new StringBuilder();
			sb.append(String
					.format("There has been a failure to deliver material to the Viz Ardome auto import location"));
			sb.append(String.format(
					"The material with ID %s has been quarrentined", pi
							.getMaterialEnvelope().getMasterID()));
			eventService.saveEvent("error", sb.toString());

		}

	}

	protected PendingImportQueue getPendingImports() {
		return pendingImports;
	}
	
	@Override
	protected boolean shouldStartAsDaemon()
	{
		return true;
	}

	@Override
	public void shutdown()
	{
		stopThread();
	}
}
