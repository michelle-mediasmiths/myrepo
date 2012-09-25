package com.mediasmiths.foxtel.placeholder;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.agent.Config.MESSAGE_PATH;
import static com.mediasmiths.foxtel.agent.Config.RECEIPT_PATH;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.validation.MayamValidator;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.foxtel.placeholder.validation.channels.ChannelValidator;
import com.mediasmiths.foxtel.placeholder.validation.channels.ChannelValidatorImpl;

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

	public PlaceholderManagerTest() throws JAXBException, SAXException {

		this.jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		this.unmarhsaller = jc.createUnmarshaller();
	}

	@Before
	public void before() throws IOException {
		mayamClient = mock(MayamClient.class);
		mayamValidator = new MayamValidator(null) {
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
						mayamClient, mayamValidator));
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

		public TestPlaceHolderMangementModule(MayamClient mc, MayamValidator mv) {
			this.mc = mc;
			this.mv = mv;
		}

		@Override
		protected void configure() {
			bind(MayamClient.class).toInstance(mc);
			bind(MayamValidator.class).toInstance(mv);
			// we are only testing the processing of a single file
			bind(new TypeLiteral<MessageProcessor<PlaceholderMessage>>() {
			}).to(SingleMessageProcessor.class);
			// we dont need to continue to monitor the message folder as we are
			// only picking up a single file for this test
			bind(DirectoryWatchingQueuer.class).to(
					PickupExistingFilesOnlyDirectoryWatcher.class);
			bind(ChannelValidator.class).to(ChannelValidatorImpl.class);
		}
	}

}
