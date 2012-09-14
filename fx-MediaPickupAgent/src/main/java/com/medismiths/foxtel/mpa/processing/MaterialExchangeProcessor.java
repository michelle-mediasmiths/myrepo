package com.medismiths.foxtel.mpa.processing;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.medismiths.foxtel.mpa.PendingImport;
import com.medismiths.foxtel.mpa.queue.PendingImportQueue;
import com.medismiths.foxtel.mpa.validation.MaterialExchangeValidator;

public class MaterialExchangeProcessor extends MessageProcessor<Material> {

	private final MayamClient mayamClient;
	
	// the lonelyMXFs and lonelyXmls collections hold files which we have seen but have not
	// yet processed as they are still awaiting a companion file
	
	// TODO: configure some way to give up on a lonely file if its partner does not arrive in N miliseconds
	private final Set<File> lonelyMXFs = new HashSet<File>();
	private final Map<File, Material> lonelyXmls = new HashMap<File, Material>();

	private final PendingImportQueue filesPendingImport;
	
	@Inject
	public MaterialExchangeProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			PendingImportQueue filesPendingImport,
			MaterialExchangeValidator messageValidator, ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller, MayamClient mayamClient,
			@Named("agent.path.failure") String failurePath,
			@Named("agent.path.archive") String archivePath) {
		super(filePathsPendingProcessing,messageValidator,receiptWriter,unmarhsaller,failurePath,archivePath);
		this.mayamClient = mayamClient;
		this.filesPendingImport = filesPendingImport;
		logger.debug("Using failure path " + failurePath);
		logger.debug("Using archivePath path " + archivePath);
	}

	private static Logger logger = Logger.getLogger(MaterialExchangeProcessor.class);
	
	@Override
	protected String getIDFromMessage(Material message) {
		String materialID = message.getTitle().getProgrammeMaterial().getMaterialID();
		logger.debug(String.format("getIDFromMessage = %s",materialID));
		return materialID;
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
	 * Called when a  validated Material is ready for processing
	 * 
	 * 
	 * Looks for the media file described by an xml file, if the media has been seen then the pair are added to a list of pending imports
	 * 
	 * If the media has not been seen then the xml file is added to a list of xml files awaiting media
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
		
		updateMamWithMaterialInformation(envelope.getMessage());	
		
		// we dont know if the xml or mxf will arrive first, and of course one
		// may arrive and not the other so we keep a list of 'lonely' files
		// until we see their partner
		// TODO: add configurable time before giving up on a partner

		File xml = envelope.getFile();
		Material material = envelope.getMessage();
		
		String xmlAbsolutePath = xml.getAbsolutePath();
		String basename = FilenameUtils.getBaseName(xmlAbsolutePath);
		String path = FilenameUtils.getPath(xmlAbsolutePath);
		File mxfFile = new File(path + System.getProperty("file.separator")
				+ basename + FilenameUtils.EXTENSION_SEPARATOR + "mxf");

		if (lonelyMXFs.contains(mxfFile)) {
			logger.info(String.format("found a media file %s for xml file %s",
					mxfFile.getAbsolutePath(), xml.getAbsolutePath()));

			// we have picked up the xml for a media file awaiting a sidecar,
			// add pending import
			PendingImport pendingImport = new PendingImport(xml, mxfFile,
					material);
			filesPendingImport.add(pendingImport);
		} else {
			logger.info(String.format(
					"Have not yet seen the media file for %s",
					xml.getAbsolutePath()));
			lonelyXmls.put(xml, material);
		}
	}

	/**
	 * Update any missing metadata for the Item in Viz Ardome with the aggregator information from the XML file
	 * @param message
	 * @throws MessageProcessingFailedException 
	 */
	private void updateMamWithMaterialInformation(Material message) throws MessageProcessingFailedException {
		updateTitle(message.getTitle());
		updateMaterial(message.getTitle().getProgrammeMaterial());
		updatePackages(message.getTitle().getProgrammeMaterial().getPresentation().getPackage());		
	}

	private void updateTitle(Title title) throws MessageProcessingFailedException {
		MayamClientErrorCode result = mayamClient.updateTitle(title);
		
		if(result != MayamClientErrorCode.SUCCESS){
			logger.error(String.format("Error updating title %s", title.getTitleID()));
			throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
		}
	}
	
	private void updateMaterial(ProgrammeMaterialType programmeMaterial) throws MessageProcessingFailedException{
		MayamClientErrorCode result =mayamClient.updateMaterial(programmeMaterial);
		
		if(result != MayamClientErrorCode.SUCCESS){
			logger.error(String.format("Error updating programme material %s", programmeMaterial.getMaterialID()));
			throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
		}
	}
	
	private void updatePackages(List<Package> packages) throws MessageProcessingFailedException{
		for(Package txPackage : packages){
			updatePackage(txPackage);
		}
	}
	
	private void updatePackage(Package txPackage) throws MessageProcessingFailedException{
		MayamClientErrorCode result = mayamClient.updatePackage(txPackage);
		
		if(result != MayamClientErrorCode.SUCCESS){
			logger.error(String.format("Error updating package %s", txPackage.getPresentationID()));
			throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_ERRORCODE);
		}
	}
	
	
	/**
	 * Called when an mxf has arrived
	 * 
	 * Looks for the medias sidecar xml, if the xml has been seen then the pair are added to a list of pending imports
	 * 
	 * If the sidecar xml has not been seen then the mxf file is added to a list of mxfs awaiting xml files
	 * 
	 * @param filePath
	 */
	@Override
	protected void processNonMessageFile(String filePath) {
		logger.info(String.format("a non xml file has arrived %s",filePath));
		
		if(! FilenameUtils.getExtension(filePath).toLowerCase(Locale.ENGLISH).equals("mxf")){
			logger.fatal("a non mxf has arrived!");
		}
		
		// we dont know if the xml or mxf will arrive first, and of course one
		// may arrive and not the other so we keep a list of 'lonely' files
		// until we see their partner
		// TODO: add configurable time before giving up on a partner
	
		File mxf = new File(filePath);
		
		String xmlAbsolutePath = mxf.getAbsolutePath();
		String basename = FilenameUtils.getBaseName(xmlAbsolutePath);
		String path = FilenameUtils.getPath(xmlAbsolutePath);
		File xmlFile = new File(path + System.getProperty("file.separator")
				+ basename + FilenameUtils.EXTENSION_SEPARATOR + "xml");

		// look for the xml file corresponding to this mxf
		if (lonelyXmls.containsKey(xmlFile)) {
			logger.info(String.format(
					"found an xml file %s for media file file %s",
					xmlFile.getAbsolutePath(), mxf.getAbsolutePath()));

			Material material = lonelyXmls.get(xmlFile);

			// add pending import
			PendingImport pendingImport = new PendingImport(xmlFile, mxf,
					material);
			filesPendingImport.add(pendingImport);
		} else {
			logger.info(String.format("Have not yet seen the xml file for %s",mxf.getAbsolutePath()));
			lonelyMXFs.add(mxf);
		}
		
	}
	
}
