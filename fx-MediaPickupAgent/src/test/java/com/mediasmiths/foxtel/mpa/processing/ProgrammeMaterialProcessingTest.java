package com.mediasmiths.foxtel.mpa.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.management.MXBean;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.mpa.ProgrammeMaterialTest;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.foxtel.mpa.validation.MediaCheck;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.foxtel.mpa.MaterialEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.guice.MediaPickupModule;
import com.mediasmiths.foxtel.mpa.processing.MatchMaker;
import com.mediasmiths.foxtel.mpa.processing.MaterialExchangeProcessor;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.foxtel.mpa.validation.MaterialExchangeValidator;

public class ProgrammeMaterialProcessingTest {

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

	/**
	 * Tests that handling of invalid messages
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws SAXException
	 * @throws JAXBException
	 * @throws DatatypeConfigurationException
	 * @throws InterruptedException
	 */
	@Test
	public void testProcessInvalidMessage() throws FileNotFoundException,
			IOException, JAXBException, SAXException,
			DatatypeConfigurationException, InterruptedException {

		// prepare files
		material = ProgrammeMaterialTest.getMaterialNoPackages(TITLE_ID,
				MATERIAL_ID);

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// prepare mocks
		when(validator.validateFile(materialXMLPath)).thenReturn(
				MessageValidationResult.FAILED_TO_UNMARSHALL);

		// queue file for processing
		filesPendingProcessingQueue.add(materialxml.getAbsolutePath());

		// wait for some time to allow processing to take place
		Thread.sleep(500l);

		// check message gets moved to failure folder
		assertFalse(materialxml.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml,
				new File(failurePath)).exists());
		
		//TODO : verfiy alerts sent

	}
	
	@Test
	public void testValidMediaAndMessageMediaFirst() throws FileNotFoundException, InterruptedException, IOException, DatatypeConfigurationException, JAXBException, SAXException{
		testProcessValidMessageAndInvalidMediaMedia(true);
	}

	@Test
	public void testValidMediaAndMessageMessageFirst() throws FileNotFoundException, InterruptedException, IOException, DatatypeConfigurationException, JAXBException, SAXException{
		testProcessValidMessageAndInvalidMediaMedia(false);
	}
	
	/**
	 * Test the handling of media that does not match the description in
	 * accompanying xml files
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws DatatypeConfigurationException
	 * @throws SAXException
	 * @throws JAXBException
	 */
	private void testProcessValidMessageAndInvalidMediaMedia(boolean mediaFirst)
			throws InterruptedException, FileNotFoundException, IOException,
			DatatypeConfigurationException, JAXBException, SAXException {
		// prepare files
		TestUtil.writeBytesToFile(100, media);
		material = ProgrammeMaterialTest.getMaterialNoPackages(TITLE_ID,
				MATERIAL_ID);

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// prepare mocks
		when(validator.validateFile(materialXMLPath)).thenReturn(
				MessageValidationResult.IS_VALID);
		when(mayamClient.updateTitle((Title) argThat(titleIDMatcher)))
				.thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.updateMaterial(argThat(materialIDMatcher)))
				.thenReturn(MayamClientErrorCode.SUCCESS);

		if(mediaFirst){
		when(matchMaker.matchMXF(media)).thenReturn(null);
		when(matchMaker.matchXML(argThat(matchEnvelopeByFile))).thenReturn(
				media.getAbsolutePath());

		}
		else{
			when(matchMaker.matchXML(argThat(matchEnvelopeByFile))).thenReturn(null);
			when(matchMaker.matchMXF(media)).thenReturn(new MaterialEnvelope(materialxml, material));			
		}
		when(mediaCheck.mediaCheck(eq(media), argThat(matchEnvelopeByFile)))
				.thenReturn(false);

		// add pending files to queue
		if (mediaFirst) {
			filesPendingProcessingQueue.add(media.getAbsolutePath());
			filesPendingProcessingQueue.add(materialxml.getAbsolutePath());
		} else {
			filesPendingProcessingQueue.add(materialxml.getAbsolutePath());
			filesPendingProcessingQueue.add(media.getAbsolutePath());
		}

		// wait for some time to allow processing to take place
		Thread.sleep(500l);

		// verfiy mocks
		verify(validator).validateFile(materialxml.getAbsolutePath());
		verify(mayamClient).updateTitle(argThat(titleIDMatcher));
		verify(mayamClient).updateMaterial(argThat(materialIDMatcher));

		InOrder inOrder = inOrder(matchMaker); // check that mxf and xml
												// processed in same order they
												// were placed in queue
		if (mediaFirst) {
			inOrder.verify(matchMaker).matchMXF(media);
			inOrder.verify(matchMaker).matchXML(argThat(matchEnvelopeByFile));
		} else {
			inOrder.verify(matchMaker).matchXML(argThat(matchEnvelopeByFile));
			inOrder.verify(matchMaker).matchMXF(media);
		}

		verify(mediaCheck).mediaCheck(eq(media), argThat(matchEnvelopeByFile));

		// check the files pending processing queue has been consumed
		assertTrue(filesPendingProcessingQueue.size() == 0);
		// check there is not pending import on the queue
		assertTrue(pendingImportQueue.size() == 0);

		// check message and material gets moved to failure folder
		assertFalse(materialxml.exists());
		assertFalse(media.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml,
				new File(failurePath)).exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(media,
				new File(failurePath)).exists());

	}

	/**
	 * Happy path testing, valid message and valid media
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws DatatypeConfigurationException
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws InterruptedException
	 */
	@Test
	public void testProcessMessageValidMessageAndMedia()
			throws FileNotFoundException, IOException,
			DatatypeConfigurationException, JAXBException, SAXException,
			InterruptedException {

		// prepare files
		TestUtil.writeBytesToFile(100, media);
		material = ProgrammeMaterialTest.getMaterialNoPackages(TITLE_ID,
				MATERIAL_ID);

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// prepare mocks
		when(validator.validateFile(materialXMLPath)).thenReturn(
				MessageValidationResult.IS_VALID);
		when(mayamClient.updateTitle((Title) argThat(titleIDMatcher)))
				.thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.updateMaterial(argThat(materialIDMatcher)))
				.thenReturn(MayamClientErrorCode.SUCCESS);

		when(matchMaker.matchMXF(media)).thenReturn(null);
		when(matchMaker.matchXML(argThat(matchEnvelopeByFile))).thenReturn(
				media.getAbsolutePath());

		when(mediaCheck.mediaCheck(eq(media), argThat(matchEnvelopeByFile)))
				.thenReturn(true);

		// add pending files to queue
		filesPendingProcessingQueue.add(media.getAbsolutePath());
		filesPendingProcessingQueue.add(materialxml.getAbsolutePath());

		// wait for some time to allow processing to take place
		Thread.sleep(500l);

		// verfiy mocks
		verify(validator).validateFile(materialxml.getAbsolutePath());
		verify(mayamClient).updateTitle(argThat(titleIDMatcher));
		verify(mayamClient).updateMaterial(argThat(materialIDMatcher));

		InOrder inOrder = inOrder(matchMaker); // check that mxf and xml
												// processed in same order they
												// were placed in queue
		inOrder.verify(matchMaker).matchMXF(media);
		inOrder.verify(matchMaker).matchXML(argThat(matchEnvelopeByFile));

		verify(mediaCheck).mediaCheck(eq(media), argThat(matchEnvelopeByFile));

		// check the files pending processing queue has been consumed
		assertTrue(filesPendingProcessingQueue.size() == 0);
		// check there is a pending import on the queue
		assertTrue(pendingImportQueue.size() == 1);
		PendingImport pi = pendingImportQueue.take();
		assertTrue(pi.getMediaFile().equals(media));
		assertTrue(pi.getMaterialEnvelope().getFile().equals(materialxml));
	}

	private ArgumentMatcher<Title> titleIDMatcher = new ArgumentMatcher<Title>() {

		@Override
		public boolean matches(Object argument) {
			return ((Title) argument).getTitleID().equals(TITLE_ID);
		}
	};

	private ArgumentMatcher<ProgrammeMaterialType> materialIDMatcher = new ArgumentMatcher<ProgrammeMaterialType>() {

		@Override
		public boolean matches(Object argument) {
			return ((ProgrammeMaterialType) argument).getMaterialID().equals(
					MATERIAL_ID);
		}
	};

	private ArgumentMatcher<MaterialEnvelope> matchEnvelopeByFile = new ArgumentMatcher<MaterialEnvelope>() {

		@Override
		public boolean matches(Object argument) {
			return ((MaterialEnvelope) argument).getFile().equals(materialxml);
		}
	};
	private Material material;

}
