package com.mediasmiths.foxtel.mpa.processing;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentMatcher;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.mpa.MaterialEnvelope;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.foxtel.mpa.guice.MediaPickupModule;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.foxtel.mpa.validation.MaterialExchangeValidator;
import com.mediasmiths.foxtel.mpa.validation.MediaCheck;
import com.mediasmiths.mayam.MayamClient;

import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;

public abstract class MaterialProcessingTest {

	MaterialExchangeProcessor processor;
	FilesPendingProcessingQueue filesPendingProcessingQueue;
	PendingImportQueue pendingImportQueue;
	MaterialExchangeValidator validator;
	ReceiptWriter receiptWriter;
	Unmarshaller unmarshaller;
	MayamClient mayamClient;
	MatchMaker matchMaker;
	String failurePath;
	String archivePath;
	String incomingPath;
	File media;
	File materialxml;
	Thread processorThread;
	MediaCheck mediaCheck;
	String materialXMLPath;

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

		filesPendingProcessingQueue = new FilesPendingProcessingQueue();
		pendingImportQueue = new PendingImportQueue();
		validator = mock(MaterialExchangeValidator.class);
		receiptWriter = mock(ReceiptWriter.class);
		unmarshaller = new MediaPickupModule().provideUnmarshaller();
		mayamClient = mock(MayamClient.class);
		matchMaker = mock(MatchMaker.class);
		mediaCheck = mock(MediaCheck.class);

		incomingPath = TestUtil.prepareTempFolder("INCOMING");
		archivePath = TestUtil.prepareTempFolder("ARCHIVE");
		failurePath = TestUtil.prepareTempFolder("FAILURE");

		media = TestUtil.getFileOfTypeInFolder("mxf", incomingPath);
		materialxml = TestUtil.getFileOfTypeInFolder("xml", incomingPath);

		processor = new MaterialExchangeProcessor(filesPendingProcessingQueue,
				pendingImportQueue, validator, receiptWriter, unmarshaller,
				mayamClient, matchMaker, mediaCheck, failurePath, archivePath);

		processorThread = new Thread(processor);
		processorThread.start();

	}

	@After
	public void after() {
		processorThread.interrupt();
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
			return argument != null && ((ProgrammeMaterialType) argument).getMaterialID().equals(
					MATERIAL_ID);
		}
	};

	protected ArgumentMatcher<MaterialEnvelope> matchEnvelopeByFile = new ArgumentMatcher<MaterialEnvelope>() {

		@Override
		public boolean matches(Object argument) {
			return argument != null && ((MaterialEnvelope) argument).getFile().equals(materialxml);
		}
	};

	protected ArgumentMatcher<Package> matchByPackageID1 = new ArgumentMatcher<Package>() {

		@Override
		public boolean matches(Object argument) {
			return argument != null && ((Package) argument).getPresentationID()
					.equals(PACKAGE_ID_1);
		}
	};

	protected ArgumentMatcher<Package> matchByPackageID2 = new ArgumentMatcher<Package>() {

		@Override
		public boolean matches(Object argument) {
			return argument != null && ((Package) argument).getPresentationID()
					.equals(PACKAGE_ID_2);
		}
	};

	protected ArgumentMatcher<Package> matchByPackageID3 = new ArgumentMatcher<Package>() {

		@Override
		public boolean matches(Object argument) {
			return argument != null && ((Package) argument).getPresentationID()
					.equals(PACKAGE_ID_3);
		}
	};

	Material material;

}
