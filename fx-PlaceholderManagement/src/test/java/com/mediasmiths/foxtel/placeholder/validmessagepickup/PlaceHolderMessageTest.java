package com.mediasmiths.foxtel.placeholder.validmessagepickup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.apache.commons.io.FilenameUtils;
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
import com.mediasmiths.foxtel.placeholder.categories.PickuptoFailure;
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
	
	protected abstract void mockValidCalls(PlaceholderMessage mesage) throws Exception;
	protected abstract void verifyValidCalls(PlaceholderMessage message) throws Exception;
	protected abstract void mockInValidCalls(PlaceholderMessage mesage) throws Exception;
	protected abstract void verifyInValidCalls(PlaceholderMessage message) throws Exception;
	
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
		mockValidCalls(message);
		//test that the generated placeholder message is valid
		assertEquals(MessageValidationResult.IS_VALID,validator.validateFile(filePath));
		
	}
	private void writePlaceHolderMessage(PlaceholderMessage message, String path) throws Exception, IOException {

		FileWriter writer = new FileWriter();
		logger.debug("writing placeholdermesage to "+path);
		writer.writeObjectToFile(message, path);
	}
	
	@Test
	@Category(PickuptoFailure.class)
	/**
	 * Tests that valid messages whos processing fails get moved to the failure folder
	 * 
	 * Extending classes should provide implementations of mockInValidCalls (which should at some point cause a failure) and verifyInValidCalls 
	 * 
	 * @throws Exception
	 */
	public final void testValidRequestThatFailsProcessesToFailure() throws Exception{
		
		String messagePath = prepareTempFolder("MESSAGE");
		String receiptPath = prepareTempFolder("RECEIPT");
		String failurePath = prepareTempFolder("FAILURE");
		
		PlaceholderMessage message = this.generatePlaceholderMessage();
		mockInValidCalls(message);
		
		String messageFilePath = writeMessageAndRunManager(message,messagePath,receiptPath,failurePath);

		verifyInValidCalls(message);
		
		//after manager has run and processed the single task the request should now be in the failure folder
		File failFile = new File(failurePath + IOUtils.DIR_SEPARATOR + FilenameUtils.getName(messageFilePath));
		File messageFile = new File(messageFilePath);
		logger.info("Looking for "+failFile.getAbsolutePath());
		assertTrue(failFile.exists());
		logger.info("Looking for "+messageFile.getAbsolutePath());
		assertFalse(messageFile.exists());
		
	}
	
	
	@Test
	@Category(PickuptoReceipt.class)
	/**
	 * Tests that a valid request gets processed an a receipt placed in the receipts folder, extending classes should implement mockValidCalls and verifyValid calls to guide  and validate the tests
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	public final void testValidRequestProcessesToReceipt() throws IOException, Exception{
		
		String messagePath = prepareTempFolder("MESSAGE");
		String receiptPath = prepareTempFolder("RECEIPT");
		String failurePath = prepareTempFolder("FAILURE");
		
		PlaceholderMessage message = this.generatePlaceholderMessage();
		mockValidCalls(message);
		
		writeMessageAndRunManager(message, messagePath, receiptPath, failurePath);

		verifyValidCalls(message);
		
		//after manager has run and processed the single task we should have a receipt		
		File receiptFile = new File(receiptPath + IOUtils.DIR_SEPARATOR +  message.getMessageID() + ".txt");	
		logger.info("Looking for "+receiptFile.getAbsolutePath());
		assertTrue(receiptFile.exists());
		
		//TODO what happens to the xml in the message folder after is has finished processing?
		
		
	}
	private String writeMessageAndRunManager(PlaceholderMessage message, String messagePath, String receiptPath, String failurePath)
			throws IOException, Exception, InterruptedException {
	
		
		String fileName = getFileName();
		writePlaceHolderMessage(message, messagePath + IOUtils.DIR_SEPARATOR + fileName);
			
    	//start up a placeholder manager to process the created message
		
		//override some properties to use temp folders
		PropertyFile propertyFile = new PropertyFile();
		propertyFile.merge(PropertyFile.find("service.properties"));
		
		Properties overridenProperties = new Properties();
		overridenProperties.put("placeholder.path.message", messagePath);
		overridenProperties.put("placeholder.path.receipt", receiptPath);
		overridenProperties.put("placeholder.path.failure", failurePath);
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
		
		return fileName;
	}
	
	
	class TestPlaceHolderMangementModule extends PlaceHolderMangementModule{
		
		private final MayamClient mc;
		public TestPlaceHolderMangementModule(MayamClient mc){
			this.mc =mc;
		}
		
		@Override
		protected void configure() {
			bind(MayamClient.class).toInstance(mc);
			//we are only testing the processing of a single file
			bind(MessageProcessor.class).to(SingleMessageProcessor.class);
			//we dont need to continue to monitor the message folder as we are only picking up a single file for this test
			bind(PlaceHolderMessageDirectoryWatcher.class).to(PickupExistingFilesOnlyDirectoryWatcher.class);
		}
	}
	
	private String prepareTempFolder(String description) throws IOException {
		//create a random folder		
		String path = FileUtils.getTempDirectoryPath() + IOUtils.DIR_SEPARATOR + RandomStringUtils.randomAlphabetic(10) + IOUtils.DIR_SEPARATOR + description;
				
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
