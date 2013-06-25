package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.FilePickUpFromDirectories;
import com.mediasmiths.foxtel.agent.queue.FilePickUpProcessingQueue;
import com.mediasmiths.foxtel.agent.queue.IFilePickup;
import com.mediasmiths.foxtel.agent.queue.SingleFilePickUp;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.channels.config.ChannelConfigModule;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.channels.config.ChannelPropertiesImpl;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.validmessagepickup.FileWriter;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.validmessagepickup.SingleMessageProcessor;
import com.mediasmiths.foxtel.placeholder.PlaceholderAgent;
import com.mediasmiths.foxtel.placeholder.guice.PlaceholderAgentModule;
import com.mediasmiths.foxtel.placeholder.validation.PlaceholderMessageValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.validation.MayamValidator;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.mock;

public abstract class PlaceholderManagerTest {

	private static Logger logger = Logger
			.getLogger(PlaceholderManagerTest.class);

	protected final JAXBContext jc;
	protected final Unmarshaller unmarhsaller;

	protected MayamClient mayamClient;
	protected MayamValidator mayamValidator;
	protected ReceiptWriter receiptWriter;
	
	protected PlaceholderMessageValidator validator;
	protected ChannelProperties channelValidator;
	protected EventService events;

	public PlaceholderManagerTest() throws JAXBException, SAXException {

		this.jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		this.unmarhsaller = jc.createUnmarshaller();
	}

	@Before
	public void before() throws IOException {
		mayamClient = mock(MayamClient.class);
		mayamValidator = new MayamValidator() {
			@Override
			public boolean validateTitleBroadcastDate(String titleID,
					XMLGregorianCalendar licenseStartDate,
					XMLGregorianCalendar licenseEndDate)
					throws MayamClientException {
				return true;
			}

			@Override
			public boolean validateMaterialBroadcastDate(
					XMLGregorianCalendar targetDate, String materialID,
					String channelTag) throws MayamClientException {
					return true;
			}
		};
		events = new EventService(){
			@Override
			public void saveEvent(String name, String payload){
				logger.info("saving event "+name);
			}

		};
		receiptWriter = mock(ReceiptWriter.class);
		channelValidator = new ChannelPropertiesImpl();
		try {
			validator = new PlaceholderMessageValidator(unmarhsaller,
					mayamClient, mayamValidator, receiptWriter,
					new SchemaValidator("PlaceholderManagement.xsd"),
					channelValidator);
		} catch (SAXException e) {
			logger.fatal("Exception constructing mesage validator", e);
		}
	}

	protected void writePlaceHolderMessage(PlaceholderMessage message,
			String path) throws Exception, IOException {

		FileWriter writer = new FileWriter();
		logger.debug("writing placeholdermesage to " + path);
		writer.writeObjectToFile(message, path);
	}

	protected void writeMessageAndRunManager(PlaceholderMessage message,
			String messagePath, String messageFileName) throws IOException,
			Exception, InterruptedException {

		writePlaceHolderMessage(message, messagePath + IOUtils.DIR_SEPARATOR
				+ messageFileName);

		// start up a placeholder manager to process the created message
		runPlaceholderManager(messagePath);
		
		Thread.sleep(2000l);

	}

	protected void runPlaceholderManager(String messagePath)
			throws InterruptedException {
		// override some properties to use temp folders
		PropertyFile propertyFile = new PropertyFile();
		propertyFile.merge(PropertyFile.find("service.properties"));

		Properties overridenProperties = new Properties();
		propertyFile.merge(overridenProperties);

		// setup guice injector
			final List<Module> moduleList = new ArrayList<Module>();
			moduleList.add(new TestPlaceHolderMangementModule(messagePath,mayamClient, mayamValidator, events));

		Injector injector = GuiceInjectorBootstrap.createInjector(propertyFile,
				new GuiceSetup() {

					@Override
					public void registerModules(List<Module> modules,
							PropertyFile config) {
						modules.addAll(moduleList);
					}

					@Override
					public void injectorCreated(Injector injector) {
					}
				});
		// run placeholder manager
		PlaceholderAgent pm = injector.getInstance(PlaceholderAgent.class);
	}

	class TestPlaceHolderMangementModule extends PlaceholderAgentModule {

		private final MayamClient mc;
		private final MayamValidator mv;
		private final EventService events;
		private final String inputPath;
		
		public TestPlaceHolderMangementModule(String inputPath,MayamClient mc, MayamValidator mv, EventService events) {
			this.mc = mc;
			this.mv = mv;
			this.events=events;
			this.inputPath=inputPath;
		}

		@Override
		protected void configure() {
			install(new ChannelConfigModule());
			bind(MayamClient.class).toInstance(mc);
			bind(MayamValidator.class).toInstance(mv);
			bind(EventService.class).toInstance(events);
			// we are only testing the processing of a single file
			bind(new TypeLiteral<MessageProcessor<PlaceholderMessage>>() {
			}).to(SingleMessageProcessor.class);
			// we dont need to continue to monitor the message folder as we are
			// only picking up a single file for this test
			bind(FilePickUpProcessingQueue.class).to(FilePickUpFromDirectories.class);
			bind(ChannelProperties.class).to(ChannelPropertiesImpl.class);
			bind(IFilePickup.class).to(SingleFilePickUp.class);
		}
		@Provides
		protected EventAPI getEventService(
				@Named("service.events.api.endpoint") final URI endpoint,
				final JAXRSProxyClientFactory clientFactory)
		{
			logger.info(String.format("events api endpoint set to %s", endpoint));
			EventAPI service = mock(EventAPI.class);

			return service;
		}

		
		@Provides
		@Named("watchfolder.locations")
		public WatchFolders provideWatchFolderLocations()
		{
		
			final List<String> locations= new ArrayList<String>(1);
			locations.add(inputPath);
			return new WatchFolders(locations);
		}
		
		@Provides
		@Named("filepickup.watched.directories")
		public File[] providePickupDirectories(){
			return new File[] { new File(inputPath)};
		}
		
		@Provides
		@Named("filepickup.watched.directories.max_file_size_bytes")
		public long provideMaxFileSize(){
			return 1024l * 1024l * 1024l;
		}
		
		@Provides
		@Named("filepickup.file.partial_pickup_timeout_interval")
		public long unmatchedTimeout(){
			return 1024l * 1024l * 1024l;
		}

	}

}
