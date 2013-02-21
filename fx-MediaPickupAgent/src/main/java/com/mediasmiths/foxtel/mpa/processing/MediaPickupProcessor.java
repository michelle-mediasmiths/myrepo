package com.mediasmiths.foxtel.mpa.processing;


import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.FilePickUpProcessingQueue;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.foxtel.mpa.validation.MediaCheck;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public abstract class MediaPickupProcessor<T> extends MessageProcessor<T>
{


	protected final MayamClient mayamClient;

	private final PendingImportQueue filesPendingImport;

	private final MediaCheck mediaCheck;

	@Inject
	@Named("ao.quarrentine.folder")
	private String aoQuarrentineFolder;
	
	// matches mxf and xml files together
	private final MatchMaker matchMaker;

	public MediaPickupProcessor(
			FilePickUpProcessingQueue filePathsPendingProcessing,
			PendingImportQueue filesPendingImport,
			MessageValidator<T> messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			Marshaller marshaller,
			MayamClient mayamClient,
			MatchMaker matchMaker,
			MediaCheck mediaCheck,
			EventService eventService)
	{
		super(filePathsPendingProcessing, messageValidator, receiptWriter, unmarhsaller, marshaller, eventService);
		this.mayamClient = mayamClient;
		this.filesPendingImport = filesPendingImport;
		this.matchMaker = matchMaker;
		this.mediaCheck = mediaCheck;
	}

	protected Logger logger = Logger.getLogger(MediaPickupProcessor.class);
	
	@Override
	protected void messageValidationFailed(String filePath,
			MessageValidationResult result) {

		logger.warn(String.format(
				"Validation of Material message %s failed for reason %s",
				FilenameUtils.getName(filePath), result.toString()));
		
		String message;
		try{
			message = FileUtils.readFileToString(new File(filePath));
		}
		catch(IOException e){
			logger.warn("IOException reading "+filePath,e);
			message = filePath;
		}
		
		try
		{
			mayamClient.createWFEErrorTaskNoAsset(FilenameUtils.getName(filePath), "Invalid Media Pickup Message Received", String.format("Failed to validate %s for reason %s",filePath,result.toString()));
		}
		catch (MayamClientException e)
		{
			logger.error("Failed to create wfe error task",e);
		}
		
		if (result == MessageValidationResult.MATERIAL_IS_NOT_PLACEHOLDER)
		{
			eventService.saveEvent("PlaceholderAlreadyHasMedia", message);
		}
		else if(result == MessageValidationResult.TITLE_DOES_NOT_EXIST || result == MessageValidationResult.MATERIAL_DOES_NOT_EXIST)
		{
			eventService.saveEvent("PlaceHolderCannotBeIdentified", message);
		}
		else if(result == MessageValidationResult.UNEXPECTED_DELIVERY_VERSION){
			eventService.saveEvent("OutOfOrder", message);
		}
		else
		{
			eventService.saveEvent("Failed", message);
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
			logger.warn("a non mxf has arrived, moving it to the failure folders"); 
			moveFileToFailureFolder(new File(filePath));
			return;
		}

		File mxf = new File(filePath);
		// try to get materialenvelop for this xml file
		MediaEnvelope materialEnvelope = matchMaker.matchMXF(mxf);

		if (materialEnvelope != null) {
			logger.info(String.format("found material description %s for mxf",
					materialEnvelope.getFile().getAbsolutePath()));
			if(materialEnvelope.isQuarrentineOnMatch()){
				logger.info("mxf detected as requiring quarrentine");
				moveToAOFolder(materialEnvelope.getFile().getAbsolutePath());
				moveToAOFolder(filePath);
			}
			else{
				createPendingImportIfValid(mxf, materialEnvelope);
			}
		} else {
			logger.debug("No matching xml file \\ material envelope found");
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
	protected void processMessage(MessageEnvelope<T> envelope)
			throws MessageProcessingFailedException {

		MamUpdateResult result = updateMamWithMaterialInformation(envelope
				.getMessage());

		if(result.isWaitForMedia()){
		
			// add masterid into a more detailed envelope
			MediaEnvelope materialEnvelope = new MediaEnvelope<T>(envelope,
					result.getMasterID());
			// try to get the mxf file for this xml
			String mxfFile = matchMaker.matchXML(materialEnvelope);
	
			if (mxfFile != null) {
				logger.info(String.format("found mxf %s for material", mxfFile));
				createPendingImportIfValid(new File(mxfFile), materialEnvelope);
			} else {
				logger.debug("No matching media found");
			}
		}
	}

	
	/**
	 * updates the mam with the material information contained in the message, returns the materials ID
	 * @param message
	 * @return
	 */
	protected abstract MamUpdateResult updateMamWithMaterialInformation(T message)throws MessageProcessingFailedException;
//
//	/**
//	 * Returned when updating mam with information from a media pickup xml. 
//	 * field MasterID holds the material\siteid of the created or updated asset
//	 *  
//	 * field waitformedia indiciates if the media pickup processor should wait for a media file or not
//	 * 
//	 * A media file will not be expected if an item already has media attatched
//	 */
	  
	 class MamUpdateResult{
		private String masterID;
		private boolean waitForMedia;
		
		public MamUpdateResult(String masterID, boolean waitForMedia){
			this.masterID = masterID;
			this.waitForMedia = waitForMedia;
		}
		
		public String getMasterID()
		{
			return masterID;
		}
		
		public boolean isWaitForMedia()
		{
			return waitForMedia;
		}
		
	}
	
	
	private void createPendingImportIfValid(File mxf,
			MediaEnvelope materialEnvelope) {

			// we have an xml and an mxf, add pending import
			PendingImport pendingImport = new PendingImport(mxf,
					materialEnvelope);

			filesPendingImport.add(pendingImport);
	}
	
	protected String getIDFromMessage(MessageEnvelope<T> envelope) {
		// TODO this is just returning the xmls file name which may not be
		// unique at all (but lets hope it is for now!)
		
		//we cant just pick out a material id as the envelope could contain marketing material
		String id = FilenameUtils.getBaseName(envelope.getFile()
				.getAbsolutePath());
		logger.debug(String.format("getIDFromMessage = %s", id));
		return id;
	}
	
	@Override
	protected boolean shouldArchiveMessages() {
		return false; // messages will be archived by Importer
	}
	
	@Override
	protected void moveFileToFailureFolder(File file){
		
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
	
	@Override
	protected void aoMismatch(File file)
	{
		Object unmarshalled = null;
		try
		{
			unmarshalled = unmarhsaller.unmarshal(file);
		}
		catch (JAXBException e)
		{
			logger.error("unmarshalling failed", e);
		}

		if (unmarshalled != null)
		{
			@SuppressWarnings("unchecked")
			T message = (T) unmarshalled;

			// wait for or find mxf to allow files to be quarrentined together

			@SuppressWarnings("rawtypes")
			MediaEnvelope materialEnvelope = new MediaEnvelope<T>(file, message, "na", true);
			// try to get the mxf file for this xml
			String mxfFile = matchMaker.matchXML(materialEnvelope);

			if (mxfFile != null)
			{
				logger.info(String.format("found mxf %s for material", mxfFile));
				moveToAOFolder(mxfFile);
				moveToAOFolder(file.getAbsolutePath());
			}
			else
			{
				logger.debug("No matching media found");
			}
		}
	}

	private void moveToAOFolder(String file)
	{
		File f = new File(file);
		try
		{
			moveFileToFolder(f, aoQuarrentineFolder, true);
		}
		catch (IOException e)
		{
			logger.warn("IOException moving file to quarrentine fodler " + file, e);
		}
	}


}
