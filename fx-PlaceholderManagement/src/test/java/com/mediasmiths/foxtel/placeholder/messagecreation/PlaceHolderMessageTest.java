package com.mediasmiths.foxtel.placeholder.messagecreation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.mediasmiths.foxtel.placeholder.PlaceHolderManager;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMangementModule;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageDirectoryWatcher;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidatorTest;
import com.mediasmiths.foxtel.placeholder.processing.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.receipt.ReceiptWriter;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;

public abstract class PlaceHolderMessageTest {

	static Logger logger = Logger.getLogger(PlaceHolderMessageTest.class);
	
	protected abstract PlaceholderMessage generatePlaceholderMessage () throws Exception;
	protected abstract String getFileName();
	
    protected MayamClient mayamClient = mock(MayamClient.class);
	protected final ReceiptWriter receiptWriter = mock(ReceiptWriter.class);
    
	private MessageValidator validator;
	
	protected String getFilePath() throws IOException{
		return "/tmp" + IOUtils.DIR_SEPARATOR + getFileName();
	}
	
	public PlaceHolderMessageTest() throws JAXBException, SAXException{
		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		validator = new MessageValidator(unmarhsaller, mayamClient,receiptWriter);

	}
	
	protected void mockCalls(PlaceholderMessage mesage) throws Exception{
		
	}
	
	protected void verifyCalls(PlaceholderMessage message){
		
	}
	
	protected Object getAction(PlaceholderMessage message){
		return message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
	}
	
	
	@Test
	public final void testWrittenPlaceHolderMessagesValidate () throws Exception {
		String filePath = getFilePath();
		PlaceholderMessage message = this.generatePlaceholderMessage();
		writePlaceHolderMessage(message,filePath);
		when(receiptWriter.receiptPathForMessageID(anyString())).thenReturn("/tmp/"+RandomStringUtils.randomAlphabetic(30));
		mockCalls(message);
		//test that the generated placeholder message is valid
		assertEquals(MessageValidationResult.IS_VALID,validator.validateFile(filePath));
		
	}
	private void writePlaceHolderMessage(PlaceholderMessage message, String path) throws Exception, IOException {

		FileWriter writer = new FileWriter();
		logger.debug("writing placeholdermesage to "+path);
		writer.writeObjectToFile(message, path);
	}
	
	@Test
	public final void testPlaceHolderPickupToReciept() throws IOException, Exception{
		
		String messagePath = prepareTempFolder();
		String receiptPath = prepareTempFolder();
		String fileName = getFileName();
		PlaceholderMessage message = this.generatePlaceholderMessage();
		writePlaceHolderMessage(message, messagePath + IOUtils.DIR_SEPARATOR + fileName);
		
		mockCalls(message);
		
    	//start up a placeholder manager to process the created message
		Injector injector = Guice.createInjector(new TestPlaceHolderMangementModule(mayamClient,messagePath,receiptPath));
		PlaceHolderManager pm = injector.getInstance(PlaceHolderManager.class);
		pm.run();
		
		verifyCalls(message);
		
		//after manager has run and processed the single task we should have a receipt		
		File receiptFile = new File(receiptPath + IOUtils.DIR_SEPARATOR +  message.getMessageID() + ".txt");	
		logger.info("Looking for "+receiptFile.getAbsolutePath());
		assertTrue(receiptFile.exists());
		
	}
	
	
	class TestPlaceHolderMangementModule extends PlaceHolderMangementModule{
		
		private final MayamClient mc;
		private final String messagepath;
		private final String receiptPath;
		
		public TestPlaceHolderMangementModule(MayamClient mc, String messagepath, String receiptPath){
			this.mc =mc;
			this.messagepath=messagepath;
			this.receiptPath = receiptPath;
		}
		
		@Override
		protected void configure() {
			bind(MayamClient.class).toInstance(mc);
			bind(MessageProcessor.class).to(SingleMessageProcessor.class);
			bind(PlaceHolderMessageDirectoryWatcher.class).to(PickupExistingFilesOnlyDirectoryWatcher.class);
			
			try {
				Properties properties = new Properties();
				properties.load(this.getClass().getClassLoader()
						.getResourceAsStream("service.properties"));
				
				//override message path so we only pick up one
				properties.put("placeholder.path.message", messagepath);
				properties.put("placeholder.path.receipt", receiptPath);
				Names.bindProperties(binder(), properties);
				
			} catch (IOException ex) {
				logger.fatal("could not load properties", ex);
				System.exit(1);
			}

		}

		
	}
	
	private String prepareTempFolder() throws IOException {
		//create a random folder		
		String path = FileUtils.getTempDirectoryPath() + RandomStringUtils.randomAlphabetic(10);
				
		File dir = new File(path);
		
		if(dir.exists()){
			FileUtils.cleanDirectory(dir);
		}
		else{
			dir.mkdirs();
		}
		
		return path;
	}
	
}
