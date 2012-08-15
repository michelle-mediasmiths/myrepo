package com.medismiths.foxtel.mpa;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.mediasmiths.FileWatcher.FileWatcherMethod;
import com.mediasmiths.foxtel.generated.Programme;

public class MediaPickupAgent extends FileWatcherMethod
{

	private static Logger logger = Logger.getLogger(MediaPickupAgent.class);

	//agent configuration, holds xsd and watch folder locations
	final MediaPickupAgentConfiguration configuration;

	//jaxbcontext and a marshaller for reading xml files
	final JAXBContext jc;
	
	//schema for validating xml files
	final Schema schema;
	final Unmarshaller unmarhsaller;

	public MediaPickupAgent(MediaPickupAgentConfiguration configuration) throws JAXBException, SAXException
	{
		this.configuration = configuration;
		this.jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated");
		this.unmarhsaller = jc.createUnmarshaller();
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schema = schemaFactory.newSchema(new StreamSource(this.getClass().getResourceAsStream(configuration.getMediaExchangeXSD())));		
	}

	/**
	 * Called when an invalid xml files arrives
	 * 
	 * @param xml
	 */
	private void handleInvalidXML(File xml, String reason)
	{
		// see : FOXTEL Content Factory - MAM Project - Phase 1 - Workflow Engine v3.1.pdf
		// 2.1.2.2 Media file is delivered without the companion XML file (or with a corrupt XML file)
		logger.fatal("Not implemented");
		//TODO: implement!
	}

	@Override
	/**
	 * Checks type of incoming files, validates xml files
	 * @see com.mediasmiths.FileWatcher.FileWatcherMethod.newFileCheck(String FilePath, String FileName)
	 */
	public void newFileCheck(String filePath, String fileName)
	{
		File file = new File(filePath + fileName);

		if (fileName.toLowerCase().endsWith(".xml"))
		{
			logger.info("An xml file has arrived");
			boolean isSchemaValid = schemaValid(file);
			onXmlArrival(file, isSchemaValid);
		}
		else if (fileName.toLowerCase().endsWith(".mxf"))
		{
			logger.info("An mxf file has arrived");
			onMXFArrival(file);
		}
		else
		{
			logger.warn("An unexpected file type has arrived");
		}
	}

	/**
	 * Called when an mxf has arrived (notified by FolderWatcher)
	 * 
	 * @param mxf
	 */
	protected void onMXFArrival(File mxf)
	{
		// notify workflow engine of medias arrival
		logger.fatal("Not implemented");
		//TODO: implement!
	}

	/**
	 * Called when an xml file arrives (notified by FolderWatcher)
	 * 
	 * @param xml
	 * @param schemaValidated
	 *            - true if the xml file matches our schema
	 */
	protected void onXmlArrival(File xml, boolean schemaValidated)
	{
		try
		{
			if (schemaValidated)
			{
				logger.info(String.format("Schema Validated xml file has arrived %s", xml.getAbsolutePath()));
				processSchemaValidXML(xml);
			}
			else
			{
				logger.info(String.format("Schema invalid xml file has arrived %s", xml.getAbsolutePath()));
				handleInvalidXML(xml, "Does not validate against schema");
			}
		}
		catch (Throwable t)
		{
			logger.fatal("Unhandled exception", t);
		}
	}

	/**
	 * Called when an xml file conforming to our xsd arrived
	 * 
	 * @param xml
	 *            - a File object referencing the xml file
	 */
	private void processSchemaValidXML(File xml)
	{

		// an xml file has arrived and it has validated against the schema so we should be able to unmarshall
		logger.debug(String.format("about to unmarshall %s", xml.getAbsolutePath()));

		try
		{
			Object unmarshalled = unmarhsaller.unmarshal(xml);
			logger.debug(String.format("unmarshalled object of type %s", unmarshalled.getClass().toString()));

			if (unmarshalled instanceof Programme)// if the supplied xml represents a programme
			{
				Programme programme = (Programme) unmarshalled;
				// notify workflow engine of programme xmls arrival
				//TODO: implement!
				logger.fatal("Not implemented");
			}
			else
			{
				logger.error(String.format("XML at %s does not describe a programme", xml));
				handleInvalidXML(xml, "Does not describe a programme");
			}
		}
		catch (JAXBException e)
		{
			logger.error(String.format("Exception unmarshalling %s", xml.getAbsolutePath()), e);
			handleInvalidXML(xml, "Unmarshalling error " + e.getMessage());
		}
	}

	private boolean schemaValid(File xml)
	{
		Validator validator = schema.newValidator();
		Source xmlFile = new StreamSource(xml);
		try
		{
			validator.validate(xmlFile);
			logger.info(String.format("xml file %s validates against schema", xml.getAbsolutePath()));
			return true;
		}
		catch (SAXException e)
		{
			logger.info(String.format("xml file %s  doesnt validate against schema", xml.getAbsolutePath()), e);
		}
		catch (IOException e)
		{
			logger.error(String.format("IOException trying to validate xml file against schema"), e);
		}

		return false;
	}

	public void start()
	{
		// start watching for files (needs more config for intervals and things, FileWatcher still in prototype)
		try
		{
			fileWatcher(configuration.getMediaFolderPath());
		}
		catch (IOException e)
		{
			logger.fatal("Could not start MediaPickupAgent",e);
		}
	}

}
