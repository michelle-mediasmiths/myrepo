package com.mediasmiths.foxtel.placeholder;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.placeholder.processing.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.receipt.ReceiptWriter;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.FileWriter;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.PickupExistingFilesOnlyDirectoryWatcher;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.SingleMessageProcessor;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;

public abstract class PlaceholderManagerTest {
	
	private static Logger logger = Logger.getLogger(PlaceholderManagerTest.class);
	
	protected final JAXBContext jc;
	protected final Unmarshaller unmarhsaller;
	
    protected MayamClient mayamClient;
	protected ReceiptWriter receiptWriter;
    
	protected MessageValidator validator;
	
	public PlaceholderManagerTest() throws JAXBException, SAXException{

		 this.jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		 this.unmarhsaller= jc.createUnmarshaller();
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
	
	protected void writePlaceHolderMessage(PlaceholderMessage message, String path) throws Exception, IOException {

		FileWriter writer = new FileWriter();
		logger.debug("writing placeholdermesage to "+path);
		writer.writeObjectToFile(message, path);
	}

	protected void writeMessageAndRunManager(PlaceholderMessage message, String messagePath, String receiptPath, String failurePath, String messageFileName)
			throws IOException, Exception, InterruptedException {
	
		writePlaceHolderMessage(message, messagePath + IOUtils.DIR_SEPARATOR + messageFileName);
			
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
	
	
}
