package com.mediasmiths.foxtel.mpa.processing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

public abstract class MediaPickupProcessor<T> extends MessageProcessor<T>
{
	protected final MayamClient mayamClient;

	@Inject
	@Named("ao.quarrentine.folder")
	private String aoQuarrentineFolder;
	
	@Inject
	private Importer importer;

	@Inject
	@Named("watchfolder.locations")
	private WatchFolders watchFolders;

	public void setWatchFolders(WatchFolders watchFolders)
	{
		this.watchFolders = watchFolders;
	}
	
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
		boolean folderIsAO = watchFolders.isAo(pp.getRootPath());

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
				|| result == MessageValidationResult.UNEXPECTED_DELIVERY_VERSION || result== MessageValidationResult.PLACEHOLDER_ALREADY_HAS_MEDIA)
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
							resultPackage.getResult()),
					isAOPickUpLocation(resultPackage.getPp()));
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

			T message = resultPackage.getMessage();
			
			switch (resultPackage.getResult())
			{
				case PLACEHOLDER_ALREADY_HAS_MEDIA:
					{
						sendPlaceholderAlreadyHasMediaEvent(pickupNotification, message);
						break;
					}
				case MATERIALID_IS_NULL_OR_EMPTY:
				{
					logger.debug("Material ID Missing");

					//boolean folderIsAO = watchFolders.isAo(pp.getRootPath());

					if(folderIsAO)
					{
						//AO
						logger.debug("Material ID Missing  AO1  test");

						eventService.saveEvent("http://www.foxtel.com.au/ip/content", "AOContentWithoutMaterialID", pickupNotification);

						logger.debug("Material ID Missing  AO1  test");
						break;
					}
					else
					{
						//NonAO
						logger.debug("Material ID Missing  1  test");

						eventService.saveEvent("http://www.foxtel.com.au/ip/content", "NonAOContentWithoutMaterialID", pickupNotification);

						logger.debug("Material ID Missing   2 test");
						break;
					}
				}
				case TITLE_DOES_NOT_EXIST:
				{

					logger.debug("No matching placeholder found for the title");

					placeholderCannotBeIdentified(pickupNotification, message,folderIsAO);
					break;
				}
				case MATERIAL_HAS_ALREADY_PASSED_PREVIEW:
				{
						if (pp.isComplete())
						{
							// media arrived for a material that already passed preview and therefore already had media
							sendPlaceholderAlreadyHasMediaEvent(pickupNotification, message);
						}
						break;
				}
				case MATERIAL_DOES_NOT_EXIST:
				{
					logger.debug("No matching placeholder found for the material");
					placeholderCannotBeIdentified(pickupNotification, message,folderIsAO);
					// eventService.saveEvent("http://www.foxtel.com.au/ip/content", "PlaceHolderCannotBeIdentified", pickupNotification);
					break;
				}
				case UNEXPECTED_DELIVERY_VERSION:
					if (pp.isComplete())
					{
						// media arrived for a material that already passed preview and therefore already had media
						sendPlaceholderAlreadyHasMediaEvent(pickupNotification, message);
					}
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
	private void placeholderCannotBeIdentified(MediaPickupNotification pickupNotification, T message,boolean folderIsAO)
	{

		//boolean folderIsAO = watchFolders.isAo(pp.getRootPath());
		if(folderIsAO)
		{
			//AO
			logger.debug("1  AO Placeholder does not exist");

			eventService.saveEvent("http://www.foxtel.com.au/ip/content", "AOPlaceHolderCannotBeIdentified", pickupNotification);

			logger.debug("2  AO Placeholder does not exist");

		}
		else
		{
			//NonAO
			logger.debug("1 Non AO Placeholder does not exist");

			eventService.saveEvent("http://www.foxtel.com.au/ip/content", "NonAOPlaceHolderCannotBeIdentified", pickupNotification);

			logger.debug("2 Non AO Placeholder does not exist");

		}

	}

	/*private void mediaImportToArdomeFailure(MediaPickupNotification pickupNotification, T message,boolean folderIsAO)
	{
		if(folderIsAO)
		{
			//AO
			logger.debug("1  AO Placeholder does not exist");

			eventService.saveEvent("http://www.foxtel.com.au/ip/content", "AOMediaImportToArdomeFailure", pickupNotification);

			logger.debug("2  AO Placeholder does not exist");

		}
		else
		{
			//NonAO
			logger.debug("1 Non AO Placeholder does not exist");

			eventService.saveEvent("http://www.foxtel.com.au/ip/content", "NonAOMediaImportToArdomeFailure", pickupNotification);

			logger.debug("2 Non AO Placeholder does not exist");

		}
	}*/
	private void sendPlaceholderAlreadyHasMediaEvent(MediaPickupNotification pickupNotification, T message)
	{
		try
		{
			try
			{
				// associate this email with channel groups
				String materialID = getMaterialIDFromMessage(message);
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
			try
			{
				AutoMatchInfo ami = getSiteIDForAutomatch(materialEnvelope);

				if (ami != null)
				{
					boolean autoMatched = mayamClient.attemptAutoMatch(ami.siteID, FilenameUtils.getBaseName(ami.fileName));
				}
				
				moveMessageToArchiveFolder(pickupPackage.getPickUp("xml"));
			}
			catch (Exception e)
			{
				logger.error("error attempting automatch for unaccompanied xml", e);
				moveFileToFailureFolder(pickupPackage.getPickUp("xml"));
			}

			// send out alert that no material arrived with this xml file
			eventService.saveEvent("warning", String.format(
					"There has been been no media received for Material message %s with MasterID %s ",
					pickupPackage.getPickUp("xml").getAbsolutePath(),
					materialEnvelope.getMasterID()));
		}
	}

	@Override
	protected void processPickupPackageNoXML(PickupPackage pp)
	{
		MediaPickupNotification pickupNotification = new MediaPickupNotification();
		pickupNotification.setFilelocation(pp.getPickUp("mxf").getAbsolutePath());
		pickupNotification.setTime((new Date()).toString());

		logger.debug("MAM-614 TEST 1");
		logger.info("received a pickup package with no xml " + pp.getRootName());
		importMediaAsUnmatched(pp.getPickUp("mxf"));
		logger.debug("MAM-614 TEST 2");

		boolean folderIsAO = watchFolders.isAo(pp.getRootPath());

		if(folderIsAO)
		{
			//AO
			logger.debug("No XML   AO1  test");

			eventService.saveEvent("http://www.foxtel.com.au/ip/content", "AOContentWithoutCompanionXML", pickupNotification);

			logger.debug("No XML   AO2  test");

		}
		else
		{
			//NonAO
			logger.debug("No XML   1  test");

			eventService.saveEvent("http://www.foxtel.com.au/ip/content", "NonAOContentWithoutCompanionXML", pickupNotification);

			logger.debug("No XML   2 test");

		}
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
