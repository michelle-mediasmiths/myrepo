package com.mediasmiths.foxtel.placeholder.messagecreation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;

public abstract class PlaceHolderMessageTest {

	static Logger logger = Logger.getLogger(PlaceHolderMessageTest.class);
	
	protected abstract PlaceholderMessage generatePlaceholderMessage () throws Exception;
	protected abstract String getFileName();
	
    protected MayamClient mayamClient = mock(MayamClient.class);
	
	private MessageValidator validator;
	
	protected String getFilePath() throws IOException{
		return "/tmp" + IOUtils.DIR_SEPARATOR + getFileName();
	}
	
	public PlaceHolderMessageTest() throws JAXBException, SAXException{
		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		validator = new MessageValidator(unmarhsaller, mayamClient);

	}
	
	protected void mockCalls() {
		
	}
	
	@Test
	public final void testWrittenPlaceHolderMessagesValidate () throws Exception {
		String filePath = getFilePath();
		writePlaceHolderMessage(filePath);
		mockCalls();
		//test that the generated placeholder message is valid
		assertEquals(MessageValidationResult.IS_VALID,validator.validateFile(filePath));
		
	}
	private void writePlaceHolderMessage(String path) throws Exception, IOException {
		PlaceholderMessage message = this.generatePlaceholderMessage();
		FileWriter writer = new FileWriter();
		logger.debug("writing placeholdermesage to "+path);
		writer.writeObjectToFile(message, path);
	}
	
//	@Test
//	public final void testPlaceHolderPickupToReciept() throws IOException, Exception{
//		
//		String dirpath = prepareTempFolder();		
//		String fileName = getFileName();
//		writePlaceHolderMessage(dirpath + IOUtils.DIR_SEPARATOR + fileName);
//		
//		mockCalls();
//		
////		PlaceHolderManagerConfiguration config = new PlaceHolderManagerConfiguration();
////		Properties p = new Properties();
////		p.put("placeholder.path.message", dirpath);
////		config.override(p);
////		
//		//start up a placeholder manager
////		PlaceHolderManager phm = new PlaceHolderManager(mayamClient, config);
////		phm.run();
//		
//	}
//	private String prepareTempFolder() throws IOException {
//		//create a random folder		
//		String path = FileUtils.getTempDirectoryPath() + RandomStringUtils.randomAlphabetic(10);
//				
//		File dir = new File(path);
//		
//		if(dir.exists()){
//			FileUtils.cleanDirectory(dir);
//		}
//		else{
//			dir.mkdirs();
//		}
//		
//		return path;
//	}
//	
}
