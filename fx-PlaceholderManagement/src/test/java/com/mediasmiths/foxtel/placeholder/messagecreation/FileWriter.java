package com.mediasmiths.foxtel.placeholder.messagecreation;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

public class FileWriter {

	/**
	 * Writes a PlaceholderMessage to an XML file and validates its structure
	 * 
	 * @param message
	 * @param filepath
	 * @throws Exception
	 */
	public void writeObjectToFile(final PlaceholderMessage message,
			final String filepath) throws Exception {

		JAXBContext context = getJAXBContext();
		Marshaller marshaller = context.createMarshaller();

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		SchemaFactory factory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = factory.newSchema(TestAddOrUpdateMaterial.class.getClassLoader()
				.getResource("PlaceholderManagement.xsd"));
		marshaller.setSchema(schema);
		
		File dir = new File(FilenameUtils.getFullPath(filepath));
		
		if(!dir.exists()){
			dir.mkdirs();
		}

		marshaller.marshal(message, new FileOutputStream(new File(filepath)));
	}

	public static JAXBContext getJAXBContext() throws Exception {

		return JAXBContext
				.newInstance(au.com.foxtel.cf.mam.pms.PlaceholderMessage.class);
	}

	/**
	 * Handles errors
	 * 
	 * @author alisonboal
	 * 
	 */
	static class MyErrorHandler implements ErrorHandler {
		@Override
		public void warning(SAXParseException exception) throws SAXException {
			System.out.println("\nWARNING");
			exception.printStackTrace();
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			System.out.println("\nERROR");
			exception.printStackTrace();
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			System.out.println("\nFATAL ERROR");
			exception.printStackTrace();
		}
	}

}
