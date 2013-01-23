package com.mediasmiths.foxtel.mpa.processing;

import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_EMERGENCY_IMPORT_FOLDER;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailureReason;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.Util;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.foxtel.mpa.validation.MaterialExchangeValidator;
import com.mediasmiths.foxtel.mpa.validation.MediaCheck;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class MaterialExchangeProcessor extends MediaPickupProcessor<Material> {

	
	@Inject
	public MaterialExchangeProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			PendingImportQueue filesPendingImport,
			MaterialExchangeValidator messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			Marshaller marshaller,
			MayamClient mayamClient,
			MatchMaker matchMaker,
			MediaCheck mediaCheck,
			@Named(ARDOME_EMERGENCY_IMPORT_FOLDER) String emergencyImportFolder,
			EventService eventService){
		super(filePathsPendingProcessing,
			filesPendingImport,
			messageValidator,
			receiptWriter,
			unmarhsaller,
			marshaller,
			mayamClient,
			matchMaker,
			mediaCheck,
			 emergencyImportFolder,
			eventService);
	}

	private static Logger logger = Logger
			.getLogger(MaterialExchangeProcessor.class);

	
	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException { // NOSONAR
		// throwing unchecked exception as hint to users of class that this
		// method is likely to throw ClassCastException

		if (!(unmarshalled instanceof Material)) {
			throw new ClassCastException(String.format(
					"unmarshalled type %s is not a Material Exchange",
					unmarshalled.getClass().toString()));
		}

	}


	/**
	 * Update any missing metadata for the Item in Viz Ardome with the
	 * aggregator information from the XML file
	 * 
	 * @param message
	 * @throws MessageProcessingFailedException
	 */
	@Override
	protected MamUpdateResult updateMamWithMaterialInformation(Material message)
			throws MessageProcessingFailedException {

		if (Util.isProgramme(message)) {
			// programme material
			updateTitle(message.getTitle());
			String materialID = message.getTitle().getProgrammeMaterial().getMaterialID();
			boolean waitForMedia = updateProgrammeMaterial(message.getTitle().getProgrammeMaterial(), message.getDetails(), message.getTitle());
			return new MamUpdateResult(materialID, waitForMedia);
		} else {
			// marketing material
			try {
				createOrUpdateTitle(message.getTitle());
				
				//not strictitly a master\materialid but a site id generated by viz ardome for the item
				String masterID = mayamClient.createMaterial(message.getTitle()
						.getTitleID(), message.getTitle()
						.getMarketingMaterial(), message.getDetails(), message.getTitle());
				return new MamUpdateResult(masterID, true);
			} catch (MayamClientException e) {
				logger.error(
						"MayamClientException update mam with marketing material information",
						e);
				throw new MessageProcessingFailedException(
						MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION,
						e);
			} catch(IllegalArgumentException iae){
				logger.error(
						"Illegal argument exception in mayam client",
						iae);
				throw new MessageProcessingFailedException(
						MessageProcessingFailureReason.MAYAM_CLIENT_ARGUMENT_EXCEPTION,
						iae);
			}

		}
	}

	/**
	 * Creates or updates a title in viz ardome, should be used for marketing
	 * material only as placeholders for programmes should exist
	 * 
	 * @param title
	 * @throws MayamClientException
	 * @throws MessageProcessingFailedException
	 */
	private void createOrUpdateTitle(Title title) throws MayamClientException,
			MessageProcessingFailedException {

		MayamClientErrorCode result;

		if (!mayamClient.titleExists(title.getTitleID())) {
			result = mayamClient.createTitle(title);
		} else {
			result = mayamClient.updateTitle(title);
		}

		if (result != MayamClientErrorCode.SUCCESS) {
			logger.error(String.format("Error creating or updating title %s",
					title.getTitleID()));
			throw new MessageProcessingFailedException(
					MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
		}
	}

	/**
	 * Update any missing metadata for the title in viz ardome with the
	 * aggregator information from the xml file
	 * 
	 * @param title
	 * @throws MessageProcessingFailedException
	 */
	private void updateTitle(Title title)
			throws MessageProcessingFailedException {
		MayamClientErrorCode result = mayamClient.updateTitle(title);

		if (result != MayamClientErrorCode.SUCCESS) {
			logger.error(String.format("Error updating title %s",
					title.getTitleID()));
			throw new MessageProcessingFailedException(
					MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
		}
	}

	/**
	 * Update any missing metadata for the material\item in viz ardome with the
	 * aggregator information
	 *
	 * returns true if media is still expected for the item
	 * 
	 * @param programmeMaterial
	 * @throws MessageProcessingFailedException
	 */
	private boolean updateProgrammeMaterial(ProgrammeMaterialType programmeMaterial, Material.Details details, Material.Title title)
			throws MessageProcessingFailedException {
		logger.trace("updatingProgrammeMaterial");

		try
		{
			boolean isPlaceholder = mayamClient.updateMaterial(programmeMaterial, details, title);
			return isPlaceholder;
		}
		catch (MayamClientException e)
		{
			logger.error(String.format("Error updating programme material %s", programmeMaterial.getMaterialID()), e);
			throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
		}
		
	}
//
//	private void updatePackages(List<Package> packages, String materialID)
//			throws MessageProcessingFailedException {
//		logger.trace("updatePackages");
//		if(packages != null)
//		for (Package txPackage : packages) {
//			updatePackage(txPackage, materialID);
//		}
//	}
//
//	/**
//	 * Update tx-package in viz ardome with information from the aggregator
//	 * 
//	 * @param txPackage
//	 * @param materialID 
//	 * @throws MessageProcessingFailedException
//	 */
//	private void updatePackage(Package txPackage, String materialID)
//			throws MessageProcessingFailedException {
//		logger.trace("updatePackage");
//		MayamClientErrorCode result = mayamClient.updatePackage(txPackage);
//
//		if (result != MayamClientErrorCode.SUCCESS) {
//			logger.error(String.format("Error updating package %s",
//					txPackage.getPresentationID()));
//			throw new MessageProcessingFailedException(
//					MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
//		}
//	}


}
