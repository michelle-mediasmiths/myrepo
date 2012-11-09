package com.mediasmiths.foxtel.placeholder;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.agent.Config.MESSAGE_PATH;
import static com.mediasmiths.foxtel.agent.Config.RECEIPT_PATH;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.placeholder.guice.PlaceholderAgentModule;
import com.mediasmiths.foxtel.placeholder.validation.PlaceholderMessageValidator;
import com.mediasmiths.foxtel.placeholder.validation.channels.ChannelValidator;
import com.mediasmiths.foxtel.placeholder.validation.channels.ChannelValidatorImpl;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.FileWriter;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.PickupExistingFilesOnlyDirectoryWatcher;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.SingleMessageProcessor;
import com.mediasmiths.mayam.AlertInterface;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.validation.MayamValidator;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.stdEvents.events.db.entity.ContentPickup;
import com.mediasmiths.stdEvents.events.db.entity.Delivery;
import com.mediasmiths.stdEvents.events.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.LogEntity;
import com.mediasmiths.stdEvents.events.db.entity.PayloadEntity;
import com.mediasmiths.stdEvents.events.db.entity.QC;
import com.mediasmiths.stdEvents.events.db.entity.Transcode;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;

public abstract class PlaceholderManagerTest {

	private static Logger logger = Logger
			.getLogger(PlaceholderManagerTest.class);

	protected final JAXBContext jc;
	protected final Unmarshaller unmarhsaller;

	protected MayamClient mayamClient;
	protected MayamValidator mayamValidator;
	protected ReceiptWriter receiptWriter;
	
	protected PlaceholderMessageValidator validator;
	protected ChannelValidator channelValidator;
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
			public boolean validateMaterialBroadcastDate(
					XMLGregorianCalendar targetDate, String materialID)
					throws MayamClientException {
				return true;
			}

			@Override
			public boolean validateTitleBroadcastDate(String titleID,
					XMLGregorianCalendar licenseStartDate,
					XMLGregorianCalendar licenseEndDate)
					throws MayamClientException {
				return true;
			}
		};
		events = new EventService(){
			@Override
			public void saveEvent(String name, String payload){
				logger.info("saving event "+name);
			}
			
			@Override
			public void saveEvent(String name, Object payload){
				logger.info("saving event "+name);
			}
		};
		receiptWriter = mock(ReceiptWriter.class);
		channelValidator = new ChannelValidatorImpl();
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
			String messagePath, String receiptPath, String failurePath,
			String archivePath, String messageFileName) throws IOException,
			Exception, InterruptedException {

		writePlaceHolderMessage(message, messagePath + IOUtils.DIR_SEPARATOR
				+ messageFileName);

		// start up a placeholder manager to process the created message
		runPlaceholderManager(messagePath, receiptPath, failurePath,
				archivePath);

	}

	protected void runPlaceholderManager(String messagePath,
			String receiptPath, String failurePath, String archivePath)
			throws InterruptedException {
		// override some properties to use temp folders
		PropertyFile propertyFile = new PropertyFile();
		propertyFile.merge(PropertyFile.find("service.properties"));

		Properties overridenProperties = new Properties();
		overridenProperties.put(MESSAGE_PATH, messagePath);
		overridenProperties.put(RECEIPT_PATH, receiptPath);
		overridenProperties.put(FAILURE_PATH, failurePath);
		overridenProperties.put(ARCHIVE_PATH, archivePath);
		propertyFile.merge(overridenProperties);

		// setup guice injector
		final List<Module> moduleList = Collections
				.<Module> singletonList(new TestPlaceHolderMangementModule(
						mayamClient, mayamValidator, events));
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
		pm.run();
	}

	class TestPlaceHolderMangementModule extends PlaceholderAgentModule {

		private final MayamClient mc;
		private final MayamValidator mv;
		private final EventService events;

		public TestPlaceHolderMangementModule(MayamClient mc, MayamValidator mv, EventService events) {
			this.mc = mc;
			this.mv = mv;
			this.events=events;
		}

		@Override
		protected void configure() {
			bind(MayamClient.class).toInstance(mc);
			bind(MayamValidator.class).toInstance(mv);
			bind(EventService.class).toInstance(events);
			// we are only testing the processing of a single file
			bind(new TypeLiteral<MessageProcessor<PlaceholderMessage>>() {
			}).to(SingleMessageProcessor.class);
			// we dont need to continue to monitor the message folder as we are
			// only picking up a single file for this test
			bind(DirectoryWatchingQueuer.class).to(
					PickupExistingFilesOnlyDirectoryWatcher.class);
			bind(ChannelValidator.class).to(ChannelValidatorImpl.class);
		}
		
		@Override
		protected EventAPI getEventService(
				@Named("service.events.api.endpoint") final URI endpoint,
				final JAXRSProxyClientFactory clientFactory)
		{
			logger.info(String.format("events api endpoint set to %s", endpoint));
			EventAPI service = mock(EventAPI.class);

			return service;
		}
	}

}
