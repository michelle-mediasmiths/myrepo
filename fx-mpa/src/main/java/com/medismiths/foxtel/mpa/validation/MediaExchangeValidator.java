package com.medismiths.foxtel.mpa.validation;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class MediaExchangeValidator
{
	
	private static Logger logger = Logger.getLogger(MediaExchangeValidator.class);
	
	final Schema schema;
	
	public MediaExchangeValidator(String schemaLocation) throws SAXException{
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schema = schemaFactory.newSchema(new StreamSource(
				this.getClass().getResourceAsStream(schemaLocation)));
	}
	
	public boolean isValid(File xml)
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
}
