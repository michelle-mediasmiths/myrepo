package com.mediasmiths.foxtel.mpa.processing;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.FilePickUpProcessingQueue;
import com.mediasmiths.foxtel.agent.queue.IFilePickup;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResultPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;

import com.mediasmiths.foxtel.ip.common.events.MediaPickupNotification;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.delivery.Importer;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

import java.util.Set;

public abstract class MediaPickupProcessor<T> extends MessageProcessor<T>
{
	protected final MayamClient mayamClient;

	@Inject
	@Named("ao.quarrentine.folder")
	private String aoQuarrentineFolder;
	
	@Inject
	private Importer importer;
	
	private UnmatchedMaterialProcessor unmatchedMaterialProcessor;

	public MediaPickupProcessor(
			IFilePickup filePickup,
			MessageValidator<T> messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			Marshaller marshaller,
			MayamClient mayamClient,
			EventService eventService,
			UnmatchedMaterialProcessor unmatchedMaterialProcessor)
	{
		super(filePickup, messageValidator, receiptWriter, unmarhsaller, marshaller, eventService);
		this.mayamClient = mayamClient;
		this.unmatchedMaterialProcessor = unmatchedMaterialProcessor;
	}

	protected Logger logger = Logger.getLogger(MediaPickupProcessor.class);

	@Override
	protected void messageValidationFailed(MessageValidationResultPackage<T> resultPackage)
	{
		final PickupPackage pp = resultPackage.getPp();

		logger.warn(String.format(
				"Validation of Material message %s failed for reason %s",
				pp.getRootName(),
				resultPackage.getResult().toString()));

		MessageValidationResult result = resultPackage.getResult();

		if (result == MessageValidationResult.AO_MISMATCH)
		{
			aoMismatch(pp);
		}
		else if (result == MessageValidationResult.MATERIAL_HAS_ALREADY_PASSED_PREVIEW
				|| result == MessageValidationResult.UNEXPECTED_DELIVERY_VERSION)
		{
			failMediaAndMessage(pp);
		}
		else
		{
			failMessageImportMediaAsUnmatched(pp);
		}

		try
		{
			mayamClient.createWFEErrorTaskNoAsset(
					pp.getRootName(),
					"Invalid Media Pickup Message Received",
					String.format(
							"Failed to validate %s for reason %s",
							pp.getPickUp("xml").getAbsolutePath(),
							resultPackage.getResult()));
		}
		catch (MayamClientException e)
		{
			logger.error("Failed to create wfe error task", e);
		}

		try
		{
			// send the event recording the issue.

			MediaPickupNotification pickupNotification = new MediaPickupNotification();
			pickupNotification.setFilelocation(pp.getPickUp("xml").getAbsolutePath());
			pickupNotification.setTime((new Date()).toString());

			switch (resultPackage.getResult())
			{
				case MATERIAL_IS_NOT_PLACEHOLDER:
					{
						try
						{
							try
							{
								// associate this email with channel groups

								@SuppressWarnings("unchecked")
								T unmarshalled = (T) unmarhsaller.unmarshal(pp.getPickUp("xml"));
								String materialID = getMaterialIDFromMessage(unmarshalled);
								Set<String> channelGroups = mayamClient.getChannelGroupsForItem(materialID);
								pickupNotification.getChannelGroup().addAll(channelGroups);
							}
							catch (Exception e)
							{
								logger.error("error determining channel groups for event", e);
							}

							eventService.saveEvent(
									"http://www.foxtel.com.au/ip/content",
									"PlaceholderAlreadyHasMedia",
									pickupNotification);
						}
						catch (Exception e)
						{
							logger.error("exception sending PlaceholderAlreadyHasMedia event");
						}
						break;
					}
				case TITLE_DOES_NOT_EXIST:
					// eventService.saveEvent("http://www.foxtel.com.au/ip/content", "ContentWithoutMasterID", pickupNotification);
					break;
				case MATERIAL_DOES_NOT_EXIST:
					// eventService.saveEvent("http://www.foxtel.com.au/ip/content", "PlaceHolderCannotBeIdentified", pickupNotification);
					break;
				case UNEXPECTED_DELIVERY_VERSION:
					// eventService.saveEvent("http://www.foxtel.com.au/ip/content", "OutOfOrder", pickupNotification);
					break;
				default:
					// eventService.saveEvent("http://www.foxtel.com.au/ip/content", "PlaceHolderCannotBeIdentified", pickupNotification);
					break;
			}
		}
		catch (Exception e)
		{
			logger.error("Unable to send events: ", e);
		}
	}

	protected abstract String getMaterialIDFromMessage(T message);

	private void failMessageImportMediaAsUnmatched(PickupPackage pp)
	{
		logger.info("XML considered failed, media will be imported as unmatched");
		if (pp.isPickedUpSuffix("xml"))
		{
			moveFileToFailureFolder(pp.getPickUp("xml"));
		}
		if (pp.isPickedUpSuffix("mxf"))
		{
			importMediaAsUnmatched(pp.getPickUp("mxf"));
		}
	}

	private void failMediaAndMessage(PickupPackage pp)
	{
		logger.info("XML and media will both be considered failed");

		if (pp.isPickedUpSuffix("xml"))
		{
			logger.trace("xml was picked up");
			moveFileToFailureFolder(pp.getPickUp("xml"));
		}

		if (pp.isPickedUpSuffix("mxf"))
		{
			logger.trace("mxf was picked up");
			moveFileToFailureFolder(pp.getPickUp("mxf"));
		}

	}

	private void aoMismatch(PickupPackage pp)
	{
		logger.info("XML and media will both be moved to ao quarrentine location");
		if (pp.isPickedUpSuffix("xml"))
		{
			logger.trace("xml was picked up");
			moveToAOFolder(pp.getPickUp("xml"));
		}
		if (pp.isPickedUpSuffix("mxf"))
		{
			logger.trace("mxf was picked up");
			moveToAOFolder(pp.getPickUp("mxf"));
		}
	}

	@Override
	protected void processingError(PickupPackage pp)
	{
		logger.info("processing failed for package, importing media as unmatched");
		if (pp.isPickedUpSuffix("xml"))
		{
			logger.trace("xml was picked up");
			moveFileToFailureFolder(pp.getPickUp("xml"));
		}
		if (pp.isPickedUpSuffix("mxf"))
		{
			logger.trace("mxf was picked up");
			importMediaAsUnmatched(pp.getPickUp("mxf"));
		}
	}

	private void importMediaCompleteMessage(MediaEnvelope<T> materialEnvelope)
	{
		// we have an xml and an mxf, add pending import
		PendingImport pendingImport = new PendingImport(materialEnvelope);
		
		logger.info("Going to attempt an import");

		try
		{
			importer.deliver(pendingImport, 1); //importer is responsible for moving both media and xml
			logger.info("Finished with import");
		}
		catch (Exception e)
		{
			logger.error("Error delivering file", e);
		}		
	}

	@Override
	protected void postProcessing(PickupPackage pp)
	{
		logger.trace("no post processing required in this agent");
	}

	private void importMediaAsUnmatched(File mediaFile)
	{
		logger.debug("Queueing file for unmatched import : " + mediaFile.getAbsolutePath());
		try
		{
			unmatchedMaterialProcessor.processUnmatchedMXF(mediaFile);
		}
		catch (IOException e)
		{
			moveFileToFailureFolder(mediaFile);
		}
	}

	/**
	 * Called when a validated Material is ready for processing
	 * 
	 * 
	 * Looks for the media file described by an xml file, if the media has been seen then the pair are added to a list of pending imports
	 * 
	 * If the media has not been seen then the xml file is added to a list of xml files awaiting media
	 * 
	 * 
	 * @param envelope
	 *            - the delivered xmlfile
	 */
	@Override
	protected void processMessage(MessageEnvelope<T> envelope) throws MessageProcessingFailedException
	{

		MamUpdateResult result = updateMamWithMaterialInformation(envelope.getMessage());

		// add masterid into a more detailed envelope
		MediaEnvelope<T> materialEnvelope = new MediaEnvelope<T>(envelope, result.getMasterID());

		final PickupPackage pickupPackage = envelope.getPickupPackage();

		if (pickupPackage.isComplete())
		{
			importMediaCompleteMessage(materialEnvelope);
		}
		else
		{
			boolean autoMatched = false;
			AutoMatchInfo ami = getSiteIDForAutomatch(materialEnvelope);

			if (ami != null)
			{
				autoMatched = mayamClient.attemptAutoMatch(ami.siteID, FilenameUtils.getBaseName(ami.fileName));
			}

			if (autoMatched)
			{
				moveMessageToArchiveFolder(pickupPackage.getPickUp("xml"));
			}
			else
			{
				moveFileToFailureFolder(pickupPackage.getPickUp("xml"));
			}

			// send out alert that no material arrived with this xml file
			eventService.saveEvent("warning", String.format(
					"There has been been no media received for Material message %s with MasterID %s ",
					pickupPackage.getPickUp("xml").getAbsolutePath(),
					materialEnvelope.getMasterID()));
		}
	}

	protected void processPickupPackageNoXML(PickupPackage pp)
	{
		logger.info("received a pickup package with no xml " + pp.getRootName());
		importMediaAsUnmatched(pp.getPickUp("mxf"));
	}

	/**
	 * updates the mam with the material information contained in the message, returns the materials ID
	 * 
	 * @param message
	 * @return
	 */
	protected abstract MamUpdateResult updateMamWithMaterialInformation(T message) throws MessageProcessingFailedException;

	//
	// /**
	// * Returned when updating mam with information from a media pickup xml.
	// * field MasterID holds the material\siteid of the created or updated asset
	// *
	// * field waitformedia indiciates if the media pickup processor should wait for a media file or not
	// *
	// * A media file will not be expected if an item already has media attatched
	// */

	class MamUpdateResult
	{
		private String masterID;

		public MamUpdateResult(String masterID)
		{
			this.masterID = masterID;
		}

		public String getMasterID()
		{
			return masterID;
		}

	}

	protected String getIDFromMessage(MessageEnvelope<T> envelope)
	{
		// this is just returning the xmls file name which may not be unique at all
		// we cant just pick out a material id as the envelope could contain marketing material
		String id = envelope.getPickupPackage().getRootName();
		logger.debug(String.format("getIDFromMessage = %s", id));
		return id;
	}

	@Override
	protected boolean shouldArchiveMessages()
	{
		return false; // messages will be archived by Importer
	}

	@Override
	protected void moveFileToFailureFolder(File file)
	{

		String message;
		try
		{
			if (FilenameUtils.getExtension(file.getAbsolutePath()).toLowerCase().equals("xml"))
			{
				message = FileUtils.readFileToString(file);
			}
			else
			{
				message = file.getAbsolutePath();
			}
		}
		catch (IOException e)
		{
			logger.warn("IOException reading " + file.getAbsolutePath(), e);
			message = file.getAbsolutePath();
		}
		eventService.saveEvent("error", message);

		super.moveFileToFailureFolder(file);
	}

	private void moveToAOFolder(File f)
	{
		try
		{
			moveFileToFolder(f, aoQuarrentineFolder, true);

			MediaPickupNotification pickupNotification = new MediaPickupNotification();
			pickupNotification.setTime((new Date()).toString());
			pickupNotification.setFilelocation(aoQuarrentineFolder + IOUtils.DIR_SEPARATOR + f.getName());

			eventService.saveEvent("http://www.foxtel.com.au/ip/content", "Quarantine", pickupNotification);
		}
		catch (IOException e)
		{
			logger.warn("IOException moving file to quarrentine fodler " + f.getAbsolutePath(), e);
		}
	}

	protected abstract AutoMatchInfo getSiteIDForAutomatch(MediaEnvelope<T> unmatchedMessage);

	class AutoMatchInfo
	{
		String siteID;
		String fileName;
	}

}
