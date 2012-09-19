package com.mediasmiths.foxtel.mpa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;

import com.mediasmiths.foxtel.generated.MaterialExchange.Material;


public class Util {

	
	public static String prepareTempFolder(String description) throws IOException {
		//create a random folder		
		String path = FileUtils.getTempDirectoryPath() + IOUtils.DIR_SEPARATOR + RandomStringUtils.randomAlphabetic(10) + IOUtils.DIR_SEPARATOR + description;
				
		path = path.replace("//", "/"); //on some systems  FileUtils.getTempDirectoryPath() returns a trailing slash and on some it does not
		
		File dir = new File(path);
		
		if(dir.exists()){
			FileUtils.cleanDirectory(dir);
		}
		else{
			dir.mkdirs();
		}
		
		return path;
	}
	
	/**
	 * Writes a PlaceholderMessage to an XML file and validates its structure
	 * 
	 * @param message
	 * @param filepath
	 * @throws Exception
	 */
	public static void writeMaterialToFile(final Material material,
			final String filepath) throws Exception {

		JAXBContext context = getJAXBContext();
		Marshaller marshaller = context.createMarshaller();

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		SchemaFactory factory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = factory.newSchema(Util.class.getClassLoader()
				.getResource("MaterialExchange_V2.0.xsd"));
		marshaller.setSchema(schema);
		
		File dir = new File(FilenameUtils.getFullPath(filepath));
		
		if(!dir.exists()){
			dir.mkdirs();
		}

		marshaller.marshal(material, new FileOutputStream(new File(filepath)));
	}

	public static JAXBContext getJAXBContext() throws Exception {

		return JAXBContext
				.newInstance(com.mediasmiths.foxtel.generated.MaterialExchange.Material.class);
	}
}
