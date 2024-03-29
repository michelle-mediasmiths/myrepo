package com.mediasmiths.foxtel.mpa.processing;

import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.WatchFolder;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.ip.common.events.FilePickUpKinds;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.foxtel.mpa.guice.MediaPickupModule;
import com.mediasmiths.foxtel.mpa.queue.MaterialExchangeFilesPendingProcessingQueue;
import com.mediasmiths.foxtel.mpa.validation.MaterialExchangeValidator;
import com.mediasmiths.mayam.MayamClient;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentMatcher;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public abstract class MaterialProcessingTest {

	MaterialExchangeProcessor processor;
	MaterialExchangeFilesPendingProcessingQueue filesPendingProcessingQueue;
	MaterialExchangeValidator validator;
	ReceiptWriter receiptWriter;
	Unmarshaller unmarshaller;
	Marshaller marshaller;
	MayamClient mayamClient;
	String failurePath;
	String archivePath;
	String incomingPath;
	String emergencyImportPath;
	File media;
	File materialxml;
	File materialXmlProcessingFile;
	String materialXMLPath;
	EventService eventService;
	FilePickUpKinds pickUpKind = FilePickUpKinds.MEDIA;
	UnmatchedMaterialProcessor unmatchedProcessor;

	final String TITLE_ID = "TITLE_ID";
	final String MATERIAL_ID = "MATERIAL_ID";
	final String PACKAGE_ID_1 = "PACKAGE_1";
	final String PACKAGE_ID_2 = "PACKAGE_2";
	final String PACKAGE_ID_3 = "PACKAGE_3";
	final String[] PACKAGE_IDS = new String[] { PACKAGE_ID_1, PACKAGE_ID_2,
			PACKAGE_ID_3 };

	@Before
	public void before() throws IOException, JAXBException,
			DatatypeConfigurationException, SAXException {

		
		validator = mock(MaterialExchangeValidator.class);
		receiptWriter = mock(ReceiptWriter.class);
		JAXBContext jc = new MediaPickupModule().provideJAXBContext();
		unmarshaller = new MediaPickupModule().provideUnmarshaller(jc);
		marshaller = new MediaPickupModule().provideMarshaller(jc,"MaterialExchange_V2.0.xsd");
		mayamClient = mock(MayamClient.class);
		eventService = mock(EventService.class);

		incomingPath = TestUtil.prepareTempFolder("INCOMING");
		archivePath =	TestUtil.createSubFolder(incomingPath, MessageProcessor.ARCHIVEFOLDERNAME);
		failurePath = TestUtil.createSubFolder(incomingPath, MessageProcessor.FAILUREFOLDERNAME);
	
		String rString = RandomStringUtils.randomAlphabetic(6);
		media = TestUtil.getFileOfTypeInFolder("mxf", incomingPath,rString);
		materialxml = TestUtil.getFileOfTypeInFolder("xml", incomingPath,rString);
	
		filesPendingProcessingQueue = new MaterialExchangeFilesPendingProcessingQueue(new File[] {new File(incomingPath)});
		
		Map<File,Long> stabilityTimes = new HashMap<File,Long>();
		stabilityTimes.put(new File(incomingPath), 100l);
		filesPendingProcessingQueue.setStabilityTimes(stabilityTimes);
		
		filesPendingProcessingQueue.setStabilityTime(100l);
		
		WatchFolders wf = new WatchFolders();
		WatchFolder w = new WatchFolder(incomingPath);
		w.setAO(false);
		w.setDelivery(archivePath);
		w.setRuzz(false);
		wf.add(w); 
		
		unmatchedProcessor = new UnmatchedMaterialProcessor(wf, eventService);
		
		processor = new MaterialExchangeProcessor(filesPendingProcessingQueue,
				 validator, receiptWriter, unmarshaller, marshaller,
				mayamClient, eventService,unmatchedProcessor);
		processor.setWatchFolders(wf);

		processor.startThread();
	}

	@After
	public void after() {
		processor.shutdown();
	}

	protected ArgumentMatcher<Title> titleIDMatcher = new ArgumentMatcher<Title>() {

		@Override
		public boolean matches(Object argument) {
			return ((Title) argument).getTitleID().equals(TITLE_ID);
		}
	};

	protected ArgumentMatcher<ProgrammeMaterialType> materialIDMatcher = new ArgumentMatcher<ProgrammeMaterialType>() {

		@Override
		public boolean matches(Object argument) {
			return argument != null
					&& ((ProgrammeMaterialType) argument).getMaterialID()
							.equals(MATERIAL_ID);
		}
	};

	protected ArgumentMatcher<MediaEnvelope> matchEnvelopeByFile = new ArgumentMatcher<MediaEnvelope>() {

		@Override
		public boolean matches(Object argument) {
			return argument != null
					&& ((MediaEnvelope) argument).getPickupPackage().getPickUp("xml").getAbsolutePath().equals(
							materialXMLPath);
		}
	};


	protected ArgumentMatcher<String> matchByMaterialID = new ArgumentMatcher<String>()
	{

		@Override
		public boolean matches(Object argument)
		{
			return argument != null && ((String) argument).equals(MATERIAL_ID);
		}
	};
	
	protected ArgumentMatcher<Package> matchByPackageID1 = new ArgumentMatcher<Package>() {

		@Override
		public boolean matches(Object argument) {
			return argument != null
					&& ((Package) argument).getPresentationID().equals(
							PACKAGE_ID_1);
		}
	};

	protected ArgumentMatcher<Package> matchByPackageID2 = new ArgumentMatcher<Package>() {

		@Override
		public boolean matches(Object argument) {
			return argument != null
					&& ((Package) argument).getPresentationID().equals(
							PACKAGE_ID_2);
		}
	};

	protected ArgumentMatcher<Package> matchByPackageID3 = new ArgumentMatcher<Package>() {

		@Override
		public boolean matches(Object argument) {
			return argument != null
					&& ((Package) argument).getPresentationID().equals(
							PACKAGE_ID_3);
		}
	};

	Material material;

}
