package com.medismiths.foxtel.mpa.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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
import com.mediasmiths.foxtel.generated.MaterialExchange.FileMediaType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.medismiths.foxtel.mpa.MaterialEnvelope;
import com.medismiths.foxtel.mpa.PendingImport;
import com.medismiths.foxtel.mpa.Util;
import com.medismiths.foxtel.mpa.queue.PendingImportQueue;
import com.medismiths.foxtel.mpa.validation.MaterialExchangeValidator;

public class MaterialExchangeProcessor extends MessageProcessor<Material> {

	private final MayamClient mayamClient;

	private final PendingImportQueue filesPendingImport;

	// matches mxf and xml files together
	private final MatchMaker matchMaker;

	@Inject
	public MaterialExchangeProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			PendingImportQueue filesPendingImport,
			MaterialExchangeValidator messageValidator,
			ReceiptWriter receiptWriter, Unmarshaller unmarhsaller,
			MayamClient mayamClient, MatchMaker matchMaker,
			@Named("agent.path.failure") String failurePath,
			@Named("agent.path.archive") String archivePath) {
		super(filePathsPendingProcessing, messageValidator, receiptWriter,
				unmarhsaller, failurePath, archivePath);
		this.mayamClient = mayamClient;
		this.filesPendingImport = filesPendingImport;
		this.matchMaker = matchMaker;
		logger.debug("Using failure path " + failurePath);
		logger.debug("Using archivePath path " + archivePath);
	}

	private static Logger logger = Logger
			.getLogger(MaterialExchangeProcessor.class);

	@Override
	protected String getIDFromMessage(MessageEnvelope<Material> envelope) {
		// TODO this is just returning the xmls file name which may not be
		// unique at all (but lets hope it is for now!)
		String id = FilenameUtils.getBaseName(envelope.getFile()
				.getAbsolutePath());
		logger.debug(String.format("getIDFromMessage = %s", id));
		return id;
	}

	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException {

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
			// we have an xml and an mxf, add pending import
			PendingImport pendingImport = new PendingImport(new File(mxfFile),
					materialEnvelope);
			filesPendingImport.add(pendingImport);
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
			logger.warn("a non mxf has arrived!");
			return;
		}

		File mxf = new File(filePath);
		// try to get materialenvelop for this xml file
		MaterialEnvelope materialEnvelope = matchMaker.matchMXF(mxf);

		if (materialEnvelope != null) {
			logger.info(String.format("found material description %s for mxf",
					materialEnvelope.getFile().getAbsolutePath()));
			// we have an xml and an mxf, add pending import
			PendingImport pendingImport = new PendingImport(mxf,
					materialEnvelope);
			filesPendingImport.add(pendingImport);
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

		// TODO notify someone of the error via email

	}

	private boolean mediaCheck(File mxf, MaterialEnvelope description) {
		// check mxf matched descriptioin.
		MaterialType material = Util.getMaterialTypeForMaterial(description
				.getMessage());

		if (!(material.getMedia() instanceof FileMediaType)) {
			logger.error("Unknown media type");
			return false;
		} else {
			FileMediaType media = (FileMediaType) material.getMedia();
			return fileSizeMatches(mxf, media) && checkSumMatches(mxf, media);
		}
		// TODO check file format? (could be quite difficult!)
	}

	protected boolean fileSizeMatches(File mxf, FileMediaType media) {

		long fileSize = mxf.length();
		long expectedSize = media.getFileSize().longValue();

		if (fileSize == expectedSize) {
			logger.debug(String.format(
					"File size of %s is %d expected size is %d",
					mxf.getAbsolutePath(), fileSize, expectedSize));
			return true;
		} else {
			logger.warn(String.format(
					"File size of %s is %d expected size is %d",
					mxf.getAbsolutePath(), fileSize, expectedSize));
			return false;
		}

	}

	protected boolean checkSumMatches(File mxf, FileMediaType media) {

		BigInteger checksum = media.getChecksum();

		try {
			if (digest.isEqual(checksum.toString(64).getBytes(),
					IOUtils.toByteArray(new FileInputStream(mxf)))) {
				// TODO : replace naive stupid implementation that reads the
				// entire file into memory (see java.nio)
				logger.debug(String.format("Checksum passes %s",
						mxf.getAbsolutePath()));
				return true;
			} else {
				logger.warn(String.format("Checksum failure %s",
						mxf.getAbsolutePath()));
				return false;
			}

		} catch (IOException e) {
			logger.error(
					String.format(
							"IOException calculating media checksum for %s, sanity check fails",
							mxf.getAbsolutePath()), e);
			return false;
		}
	}

	@Named("media.digest.algorithm")
	private final MessageDigest digest = DigestUtils.getDigest("md5"); // for
																		// validating
																		// file
																		// checksums

}
