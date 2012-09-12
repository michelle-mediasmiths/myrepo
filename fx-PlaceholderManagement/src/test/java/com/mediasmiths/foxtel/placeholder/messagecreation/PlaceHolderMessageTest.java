package com.mediasmiths.foxtel.placeholder.messagecreation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.placeholder.PlaceHolderManager;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMangementModule;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageDirectoryWatcher;
import com.mediasmiths.foxtel.placeholder.categories.MessageCreation;
import com.mediasmiths.foxtel.placeholder.categories.PickuptoReceipt;
import com.mediasmiths.foxtel.placeholder.processing.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.receipt.ReceiptWriter;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;

public abstract class PlaceHolderMessageTest {

	static Logger logger = Logger.getLogger(PlaceHolderMessageTest.class);
	
	protected abstract PlaceholderMessage generatePlaceholderMessage () throws Exception;
	protected abstract String getFileName();
	
	protected final JAXBContext jc;
	protected final Unmarshaller unmarhsaller;
	
    protected MayamClient mayamClient;
	protected ReceiptWriter receiptWriter;
    
	private MessageValidator validator;
	
	protected String getFilePath() throws IOException{
		return "/tmp" + IOUtils.DIR_SEPARATOR + getFileName();
	}
	
	@Before
	public void before(){
		mayamClient = mock(MayamClient.class);
		receiptWriter = mock(ReceiptWriter.class);
		try {
			validator = new MessageValidator(unmarhsaller, mayamClient,receiptWriter);
		} catch (SAXException e) {
			logger.fatal("Exception constructing mesage validator",e);
		}
	}
	
	
	public PlaceHolderMessageTest() throws JAXBException, SAXException{

		 this.jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		 this.unmarhsaller= jc.createUnmarshaller();
	}
	
	protected void mockCalls(PlaceholderMessage mesage) throws Exception{
		
	}
	
	protected void verifyCalls(PlaceholderMessage message){
		
	}
	
	protected Object getAction(PlaceholderMessage message){
		return message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
	}
	
	
	@Test
	@Category(MessageCreation.class)
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
	@Category(PickuptoReceipt.class)
	public final void testPlaceHolderPickupToReciept() throws IOException, Exception{
		
		String messagePath = prepareTempFolder();
		String receiptPath = prepareTempFolder();
		String fileName = getFileName();
		PlaceholderMessage message = this.generatePlaceholderMessage();
		writePlaceHolderMessage(message, messagePath + IOUtils.DIR_SEPARATOR + fileName);
		
		mockCalls(message);
		
    	//start up a placeholder manager to process the created message
		
		//override some properties to use temp folders
		PropertyFile propertyFile = new PropertyFile();
		propertyFile.merge(PropertyFile.find("service.properties"));
		
		Properties overridenProperties = new Properties();
		overridenProperties.put("placeholder.path.message", messagePath);
		overridenProperties.put("placeholder.path.receipt", receiptPath);
		propertyFile.merge(overridenProperties);
		
		//setup guice injector
		final List<Module> moduleList = Collections.<Module>singletonList(new TestPlaceHolderMangementModule(mayamClient));
		Injector injector = GuiceInjectorBootstrap.createInjector(propertyFile,new GuiceSetup() {
			
			@Override
			public void registerModules(List<Module> modules, PropertyFile config) {
				modules.addAll(moduleList);
			}
			@Override
			public void injectorCreated(Injector injector) {
			}
		});		
		//run placeholder manager
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
		public TestPlaceHolderMangementModule(MayamClient mc){
			this.mc =mc;
		}
		
		@Override
		protected void configure() {
			bind(MayamClient.class).toInstance(mc);
			bind(MessageProcessor.class).to(SingleMessageProcessor.class);
			bind(PlaceHolderMessageDirectoryWatcher.class).to(PickupExistingFilesOnlyDirectoryWatcher.class);
		}
	}
	
	private String prepareTempFolder() throws IOException {
		//create a random folder		
		String path = FileUtils.getTempDirectoryPath() + IOUtils.DIR_SEPARATOR + RandomStringUtils.randomAlphabetic(10);
				
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
