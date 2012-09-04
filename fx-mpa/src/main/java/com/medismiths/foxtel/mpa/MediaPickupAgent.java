package com.medismiths.foxtel.mpa;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.mediasmiths.FileWatcher.FileWatcherMethod;
import com.mediasmiths.foxtel.generated.MediaExchange.Programme;
import com.medismiths.foxtel.mpa.config.MediaPickupAgentConfiguration;
import com.medismiths.foxtel.mpa.delivery.Importer;
import com.medismiths.foxtel.mpa.validation.MediaExchangeValidator;

public class MediaPickupAgent extends FileWatcherMethod {

	private static Logger logger = Logger.getLogger(MediaPickupAgent.class);

	// agent configuration, holds xsd and watch folder locations
	private final MediaPickupAgentConfiguration configuration;

	// jaxbcontext and a marshaller for reading xml files
	private final JAXBContext jc;
	private final Unmarshaller unmarhsaller;

	// validates incoming xml against schema
	private final MediaExchangeValidator mediaExchangeValidator;

	// the lonelyMXFs and lonelyXmls collections hold files which we have seen but have not
	// yet processed as they are still awaiting a companion file
	
	// TODO: configure some way to give up on a lonely file if its partner does not arrive in N miliseconds
	private final Set<File> lonelyMXFs = new HashSet<File>();
	private final Map<File, Programme> lonelyXmls = new HashMap<File, Programme>();

	private final Importer importer = new Importer();
	private final Thread importerThread = new Thread(importer);

	public MediaPickupAgent(MediaPickupAgentConfiguration configuration)
			throws JAXBException, SAXException {
		this.configuration = configuration;
		this.jc = JAXBContext
				.newInstance("com.mediasmiths.foxtel.generated.MediaExchange");
		this.unmarhsaller = jc.createUnmarshaller();
		this.mediaExchangeValidator = new MediaExchangeValidator(
				configuration.getMediaExchangeXSD());
	}

	/**
	 * Starts the agent
	 */
	public void start() {

		// starts the importer which loops awaiting notification of pending
		// imports
		importerThread.start();

		// start watching for files (needs more config for intervals and things,
		// FileWatcher still in prototype)
		try {
			fileWatcher(configuration.getMediaFolderPath());
		} catch (IOException e) {
			logger.fatal("Could not start MediaPickupAgent", e);
		}
	}

	@Override
	/**
	 * Checks type of incoming files, validates xml files
	 * @see com.mediasmiths.FileWatcher.FileWatcherMethod.newFileCheck(String FilePath, String FileName)
	 */
	public void newFileCheck(String filePath, String fileName) {
		File file = new File(filePath + fileName);

		if (fileName.toLowerCase().endsWith(".xml")) {
			logger.info("An xml file has arrived");
			boolean isSchemaValid = mediaExchangeValidator.isValid(file);
			onXmlArrival(file, isSchemaValid);
		} else if (fileName.toLowerCase().endsWith(".mxf")) {
			logger.info("An mxf file has arrived");
			onMXFArrival(file);
		} else {
			logger.warn("An unexpected file type has arrived");
		}
	}

	/**
	 * Called when an xml file arrives (notified by FolderWatcher)
	 * 
	 * @param xml
	 * @param schemaValidated
	 *            - true if the xml file matches our schema
	 */
	protected void onXmlArrival(File xml, boolean schemaValidated) {
		try {
			if (schemaValidated) {
				logger.info(String.format(
						"Schema Validated xml file has arrived %s",
						xml.getAbsolutePath()));
				processSchemaValidXML(xml);
			} else {
				logger.info(String.format(
						"Schema invalid xml file has arrived %s",
						xml.getAbsolutePath()));
				handleInvalidXML(xml, "Does not validate against schema");
			}
		} catch (Throwable t) {
			logger.fatal("Unhandled exception", t);
		}
	}

	/**
	 * Called when an xml file conforming to our xsd arrived
	 * 
	 * @param xml
	 *            - a File object referencing the xml file
	 */
	private void processSchemaValidXML(File xml) {

		// an xml file has arrived and it has validated against the schema so we
		// should be able to unmarshall
		logger.debug(String.format("about to unmarshall %s",
				xml.getAbsolutePath()));

		try {
			Object unmarshalled = unmarshallFile(xml);

			if (unmarshalled instanceof Programme)// if the supplied xml
													// represents a programme
			{
				Programme programme = (Programme) unmarshalled;
				onProgrammeXmlArrival(programme, xml);
			}

			// TODO : 2.2.2.1 TNS File Delivery – Associated Content
			// else if unmarshalled instance of (blackspot metadata, companion
			// assets metadata (commerial??))

			else {
				logger.error(String.format(
						"XML at %s does not describe a programme", xml));
				handleInvalidXML(xml, "Does not describe a programme");
			}
		} catch (JAXBException e) {
			logger.error(
					String.format("Exception unmarshalling %s",
							xml.getAbsolutePath()), e);
			handleInvalidXML(xml, "Unmarshalling error " + e.getMessage());
		}
	}

	private Object unmarshallFile(File xml) throws JAXBException {
		Object unmarshalled = unmarhsaller.unmarshal(xml);
		logger.debug(String.format("unmarshalled object of type %s",
				unmarshalled.getClass().toString()));
		return unmarshalled;
	}

	/**
	 * Called when a programme arrives
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
	private synchronized void onProgrammeXmlArrival(Programme programme,
			File xml) {

		logger.fatal("onProgrammeXmlArrival Not implemented");

		// we dont know if the xml or mxf will arrive first, and of course one
		// may arrive and not the other so we keep a list of 'lonely' files
		// until we see their partner
		// TODO: add configurable time before giving up on a partner

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
					programme);
			importer.addPendingImport(pendingImport);
		} else {
			logger.info(String.format(
					"Have not yet seen the media file for %s",
					xml.getAbsolutePath()));
			lonelyXmls.put(xml, programme);
		}

	}

	/**
	 * Called when an mxf has arrived (notified by FolderWatcher)
	 * 
	 * Looks for the medias sidecar xml, if the xml has been seen then the pair are added to a list of pending imports
	 * 
	 * If the sidecar xml has not been seen then the mxf file is added to a list of mxfs awaiting xml files
	 * 
	 * @param mxf
	 */
	protected synchronized void onMXFArrival(File mxf) {
		logger.fatal("onMXFArrival Not implemented");

		// we dont know if the xml or mxf will arrive first, and of course one
		// may arrive and not the other so we keep a list of 'lonely' files
		// until we see their partner
		// TODO: add configurable time before giving up on a partner
	
		
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

			Programme programme = lonelyXmls.get(xmlFile);

			// add pending import
			PendingImport pendingImport = new PendingImport(xmlFile, mxf,
					programme);
			importer.addPendingImport(pendingImport);
		} else {
			logger.info(String.format("Have not yet seen the xml file for %s",mxf.getAbsolutePath()));
			lonelyMXFs.add(mxf);
		}

	}

	/**
	 * Called when an invalid xml files arrives
	 * 
	 * @param xml
	 */
	private void handleInvalidXML(File xml, String reason) {
		// see : FOXTEL Content Factory - MAM Project - Phase 1 - Workflow
		// Engine v3.1.pdf
		// 2.1.2.2 Media file is delivered without the companion XML file (or
		// with a corrupt XML file)
		logger.fatal("handleInvalidXML Not implemented");
		// TODO: handleInvalidXML
	}

}
