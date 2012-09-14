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
import com.google.inject.TypeLiteral;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.placeholder.guice.PlaceholderAgentModule;
import com.mediasmiths.foxtel.placeholder.validation.PlaceholderMessageValidator;
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
    
	protected PlaceholderMessageValidator validator;
	
	public PlaceholderManagerTest() throws JAXBException, SAXException{

		 this.jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		 this.unmarhsaller= jc.createUnmarshaller();
	}
	
	@Before
	public void before(){
		mayamClient = mock(MayamClient.class);
		receiptWriter = mock(ReceiptWriter.class);
		try {
			validator = new PlaceholderMessageValidator(unmarhsaller, mayamClient,receiptWriter, new SchemaValidator("PlaceholderManagement.xsd"));
		} catch (SAXException e) {
			logger.fatal("Exception constructing mesage validator",e);
		}
	}
	
	protected void writePlaceHolderMessage(PlaceholderMessage message, String path) throws Exception, IOException {

		FileWriter writer = new FileWriter();
		logger.debug("writing placeholdermesage to "+path);
		writer.writeObjectToFile(message, path);
	}

	protected void writeMessageAndRunManager(PlaceholderMessage message, String messagePath, String receiptPath, String failurePath, String archivePath, String messageFileName)
			throws IOException, Exception, InterruptedException {
	
		writePlaceHolderMessage(message, messagePath + IOUtils.DIR_SEPARATOR + messageFileName);
			
    	//start up a placeholder manager to process the created message		
		runPlaceholderManager(messagePath, receiptPath, failurePath,archivePath);		
		
	}

	protected void runPlaceholderManager(String messagePath, String receiptPath,
			String failurePath, String archivePath) throws InterruptedException {
		//override some properties to use temp folders
		PropertyFile propertyFile = new PropertyFile();
		propertyFile.merge(PropertyFile.find("service.properties"));
		
		Properties overridenProperties = new Properties();
		overridenProperties.put("agent.path.message", messagePath);
		overridenProperties.put("agent.path.receipt", receiptPath);
		overridenProperties.put("agent.path.failure", failurePath);
		overridenProperties.put("agent.path.archive", archivePath);
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
		PlaceholderAgent pm = injector.getInstance(PlaceholderAgent.class);
		pm.run();
	}
	
	
	class TestPlaceHolderMangementModule extends PlaceholderAgentModule{
		
		private final MayamClient mc;
		public TestPlaceHolderMangementModule(MayamClient mc){
			this.mc =mc;
		}
		
		@Override
		protected void configure() {
			bind(MayamClient.class).toInstance(mc);
			//we are only testing the processing of a single file
			bind(new TypeLiteral<MessageProcessor<PlaceholderMessage>>(){}).to(SingleMessageProcessor.class);
			//we dont need to continue to monitor the message folder as we are only picking up a single file for this test
			bind(DirectoryWatchingQueuer.class).to(PickupExistingFilesOnlyDirectoryWatcher.class);
		}
	}
	
	
}
