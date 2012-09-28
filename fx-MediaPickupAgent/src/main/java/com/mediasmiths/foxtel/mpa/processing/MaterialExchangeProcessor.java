package com.mediasmiths.foxtel.mpa.processing;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_FAILURE_ALERT_RECIPIENT;

import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailureReason;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.mpa.MaterialEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.Util;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.foxtel.mpa.validation.MaterialExchangeValidator;
import com.mediasmiths.foxtel.mpa.validation.MediaCheck;
import com.mediasmiths.mayam.AlertInterface;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class MaterialExchangeProcessor extends MessageProcessor<Material> {

	private final MayamClient mayamClient;

	private final PendingImportQueue filesPendingImport;

	private final MediaCheck mediaCheck;

	// matches mxf and xml files together
	private final MatchMaker matchMaker;

	private final AlertInterface alert;
	private final String deliveryFailureAlertReceipient;

	@Inject
	public MaterialExchangeProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			PendingImportQueue filesPendingImport,
			MaterialExchangeValidator messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			MayamClient mayamClient,
			MatchMaker matchMaker,
			MediaCheck mediaCheck,
			@Named(FAILURE_PATH) String failurePath,
			@Named(ARCHIVE_PATH) String archivePath,
			AlertInterface alert,
			@Named(DELIVERY_FAILURE_ALERT_RECIPIENT) String deliveryFailureAlertReceipient) {
		super(filePathsPendingProcessing, messageValidator, receiptWriter,
				unmarhsaller, failurePath, archivePath);
		this.mayamClient = mayamClient;
		this.filesPendingImport = filesPendingImport;
		this.matchMaker = matchMaker;
		this.mediaCheck = mediaCheck;
		this.alert = alert;
		this.deliveryFailureAlertReceipient = deliveryFailureAlertReceipient;
		logger.debug("Using failure path " + failurePath);
		logger.debug("Using archivePath path " + archivePath);
	}

	private static Logger logger = Logger
			.getLogger(MaterialExchangeProcessor.class);

	@Override
	protected String getIDFromMessage(MessageEnvelope<Material> envelope) {
		// TODO this is just returning the xmls file name which may not be
		// unique at all (but lets hope it is for now!)
		
		//we cant just pick out a material id as the envelope could contain marketing material
		String id = FilenameUtils.getBaseName(envelope.getFile()
				.getAbsolutePath());
		logger.debug(String.format("getIDFromMessage = %s", id));
		return id;
	}

	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException { // NOSONAR
		// throwing unchecked exception as hint to users of class that this
		// method is likely to throw ClassCastException

		if (!(unmarshalled instanceof Material)) {
			throw new ClassCastException(String.format(
					"unmarshalled type %s is not a PlaceholderMessage",
					unmarshalled.getClass().toString()));
		}

	}

	/**
	 * Called when a validated Material is ready for processing
	 * 
	 * 
	 * Looks for the media file described by an xml file, if the media has been
	 * seen then the pair are added to a list of pending imports
	 * 
	 * If the media has not been seen then the xml file is added to a list of
	 * xml files awaiting media
	 * 
	 * 
	 * @param programme
	 *            - the unmarshalled xml
	 * @param xml
	 *            - the delivered xmlfile
	 */
	@Override
	protected void processMessage(MessageEnvelope<Material> envelope)
			throws MessageProcessingFailedException {

		String masterID = updateMamWithMaterialInformation(envelope
				.getMessage());

		// add masterid into a more detailed envelope
		MaterialEnvelope materialEnvelope = new MaterialEnvelope(envelope,
				masterID);
		// try to get the mxf file for this xml
		String mxfFile = matchMaker.matchXML(materialEnvelope);

		if (mxfFile != null) {
			logger.info(String.format("found mxf %s for material", mxfFile));
			createPendingImportIfValid(new File(mxfFile), materialEnvelope);
		} else {
			logger.debug("No matching media found");
		}
	}

	/**
	 * Called when an mxf has arrived
	 * 
	 * Looks for the medias sidecar xml, if the xml has been seen then the pair
	 * are added to a list of pending imports
	 * 
	 * If the sidecar xml has not been seen then the mxf file is added to a list
	 * of mxfs awaiting xml files
	 * 
	 * @param filePath
	 */
	@Override
	protected void processNonMessageFile(String filePath) {
		logger.info(String.format("a non xml file has arrived %s", filePath));

		if (!FilenameUtils.getExtension(filePath).toLowerCase(Locale.ENGLISH)
				.equals("mxf")) {
			logger.warn("a non mxf has arrived!"); // this really shouldn't
													// happen if the
													// MaterialFolderWatcher is
													// doing its job right
			return;
		}

		File mxf = new File(filePath);
		// try to get materialenvelop for this xml file
		MaterialEnvelope materialEnvelope = matchMaker.matchMXF(mxf);

		if (materialEnvelope != null) {
			logger.info(String.format("found material description %s for mxf",
					materialEnvelope.getFile().getAbsolutePath()));
			createPendingImportIfValid(mxf, materialEnvelope);
		} else {
			logger.debug("No matching xml file \\ material envelope found");
		}
	}

	private void createPendingImportIfValid(File mxf,
			MaterialEnvelope materialEnvelope) {

		if (mediaCheck.mediaCheck(mxf, materialEnvelope)) {

			// we have an xml and an mxf, add pending import
			PendingImport pendingImport = new PendingImport(mxf,
					materialEnvelope);

			filesPendingImport.add(pendingImport);
		} else {
			logger.error("Media check failed");
			moveFileToFailureFolder(mxf);
			moveFileToFailureFolder(materialEnvelope.getFile());
			

			// send out alert that there has been an error
			StringBuilder sb = new StringBuilder();
			sb.append(String.format(
					"Media check of Material %s failed",
					FilenameUtils.getName(mxf.getAbsolutePath())));
			alert.sendAlert(deliveryFailureAlertReceipient, "Media Pickup Failure",
					sb.toString());
			
		}
	}

	/**
	 * Update any missing metadata for the Item in Viz Ardome with the
	 * aggregator information from the XML file
	 * 
	 * @param message
	 * @throws MessageProcessingFailedException
	 */
	private String updateMamWithMaterialInformation(Material message)
			throws MessageProcessingFailedException {

		if (Util.isProgramme(message)) {
			// programme material
			updateTitle(message.getTitle());
			updateProgrammeMaterial(message.getTitle().getProgrammeMaterial());
			if (message.getTitle().getProgrammeMaterial().getPresentation() != null) {

				updatePackages(message.getTitle().getProgrammeMaterial()
						.getPresentation().getPackage());
			}
			return message.getTitle().getProgrammeMaterial().getMaterialID();
		} else {
			// marketing material
			try {
				createOrUpdateTitle(message.getTitle());
				String masterID = mayamClient.createMaterial(message.getTitle()
						.getTitleID(), message.getTitle()
						.getMarketingMaterial());
				return masterID;
			} catch (MayamClientException e) {
				logger.error(
						"MayamClientException update mam with marketing material information",
						e);
				throw new MessageProcessingFailedException(
						MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION,
						e);
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
	 * @param programmeMaterial
	 * @throws MessageProcessingFailedException
	 */
	private void updateProgrammeMaterial(ProgrammeMaterialType programmeMaterial)
			throws MessageProcessingFailedException {
		logger.trace("updatingProgrammeMaterial");

		MayamClientErrorCode result = mayamClient
				.updateMaterial(programmeMaterial);

		if (result != MayamClientErrorCode.SUCCESS) {
			logger.error(String.format("Error updating programme material %s",
					programmeMaterial.getMaterialID()));
			throw new MessageProcessingFailedException(
					MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
		}
	}

	private void updatePackages(List<Package> packages)
			throws MessageProcessingFailedException {
		logger.trace("updatePackages");
		for (Package txPackage : packages) {
			updatePackage(txPackage);
		}
	}

	/**
	 * Update tx-package in viz ardome with information from the aggregator
	 * 
	 * @param txPackage
	 * @throws MessageProcessingFailedException
	 */
	private void updatePackage(Package txPackage)
			throws MessageProcessingFailedException {
		logger.trace("updatePackage");
		MayamClientErrorCode result = mayamClient.updatePackage(txPackage);

		if (result != MayamClientErrorCode.SUCCESS) {
			logger.error(String.format("Error updating package %s",
					txPackage.getPresentationID()));
			throw new MessageProcessingFailedException(
					MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
		}
	}

	@Override
	protected boolean shouldArchiveMessages() {
		return false; // messages will be archived by Importer
	}

	@Override
	protected void messageValidationFailed(String filePath,
			MessageValidationResult result) {

		// send out alert that there has been an error validating a material
		// message
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(
				"Validation of Material message %s failed for reason %s",
				FilenameUtils.getName(filePath), result.toString()));
		alert.sendAlert(deliveryFailureAlertReceipient, "Media Pickup Failure",
				sb.toString());

	}
}
