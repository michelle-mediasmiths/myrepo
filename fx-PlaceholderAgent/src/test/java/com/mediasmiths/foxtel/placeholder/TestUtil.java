package com.mediasmiths.foxtel.placeholder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Ignore;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.generated.MaterialExchange.Material;

@Ignore //this is not a test class but maven thinks it is from the name
public class TestUtil {

	
	public static String prepareTempFolder(String description) throws IOException {
		//create a random folder		
		//String path = FileUtils.getTempDirectoryPath() + IOUtils.DIR_SEPARATOR + RandomStringUtils.randomAlphabetic(10) + IOUtils.DIR_SEPARATOR + description;
		String path="/tmp/mediaTestData/"+IOUtils.DIR_SEPARATOR  + description;
		
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
	
	public static String createSubFolder(String parentPath, String name) throws IOException{
		
		String path = parentPath + IOUtils.DIR_SEPARATOR + name;
		
		File dir = new File(path);
		
		if(dir.exists()){
			FileUtils.cleanDirectory(dir);
		}
		else{
			dir.mkdirs();
		}
		
		return path;	
		
	}
	
	public static File getFileOfTypeInFolder(String extension, String folder){
		return new File(folder + IOUtils.DIR_SEPARATOR + "CreateFileInfolder"+RandomStringUtils.randomAlphabetic(6) + FilenameUtils.EXTENSION_SEPARATOR + extension);
	}
	
	public static File getPathToThisFileIfItWasInThisFolder(File file, File folder){
		String path = folder.getAbsolutePath();
		String filename = FilenameUtils.getName(file.getAbsolutePath());
		
		return new File(path + IOUtils.DIR_SEPARATOR + filename);
	}
	
	public static void writeBytesToFile(int count, File f) throws FileNotFoundException, IOException{
		IOUtils.write(new byte[count], new FileOutputStream(f));
	}
		
	/**
	 * Writes a PlaceholderMessage to an XML file and validates its structure
	 * 
	 * @param message
	 * @param filepath
	 * @throws JAXBException 
	 * @throws SAXException 
	 * @throws FileNotFoundException 
	 * @throws Exception
	 */
	public static void writeMaterialToFile(final Material material,
			final String filepath) throws JAXBException, SAXException, FileNotFoundException     {

		JAXBContext context = getJAXBContext();
		Marshaller marshaller = context.createMarshaller();

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		SchemaFactory factory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = factory.newSchema(TestUtil.class.getClassLoader()
				.getResource("MaterialExchange_V2.0.xsd"));
		marshaller.setSchema(schema);
		
		File dir = new File(FilenameUtils.getFullPath(filepath));
		
		if(!dir.exists()){
			dir.mkdirs();
		}

		marshaller.marshal(material, new FileOutputStream(new File(filepath)));
	}

	public static JAXBContext getJAXBContext() throws JAXBException   {

		return JAXBContext
				.newInstance(com.mediasmiths.foxtel.generated.MaterialExchange.Material.class);
	}
}
