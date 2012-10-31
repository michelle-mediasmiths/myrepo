package com.mediasmiths.foxtel.mpa.processing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Test;
import org.mockito.InOrder;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.mpa.MarketingMaterialTest;
import com.mediasmiths.foxtel.mpa.MaterialEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class MarketingMaterialProcessingTest extends MaterialProcessingTest {

	@Test
	public void testProcessMessageValidMessageAndMediaTitleExistsMediaFirstMediaValid()
			throws FileNotFoundException, DatatypeConfigurationException,
			IOException, JAXBException, SAXException, InterruptedException,
			MayamClientException {
		testProcessMessageValidMessageAndMedia(true, true, true);
	}

	@Test
	public void testProcessMessageValidMessageAndMediaTitleExistsMediaFirstMediaInValid()
			throws FileNotFoundException, DatatypeConfigurationException,
			IOException, JAXBException, SAXException, InterruptedException,
			MayamClientException {
		testProcessMessageValidMessageAndMedia(true, true, false);
	}

	@Test
	public void testProcessMessageValidMessageAndMediaTitleExistsMessageFirstMediaValid()
			throws FileNotFoundException, DatatypeConfigurationException,
			IOException, JAXBException, SAXException, InterruptedException,
			MayamClientException {
		testProcessMessageValidMessageAndMedia(true, false, true);
	}

	@Test
	public void testProcessMessageValidMessageAndMediaTitleExistsMessageFirstMediaInvalid()
			throws FileNotFoundException, DatatypeConfigurationException,
			IOException, JAXBException, SAXException, InterruptedException,
			MayamClientException {
		testProcessMessageValidMessageAndMedia(true, false, false);
	}

	@Test
	public void testProcessMessageValidMessageAndMediatitleDoesntExistMediaFirstMediaValid()
			throws FileNotFoundException, DatatypeConfigurationException,
			IOException, JAXBException, SAXException, InterruptedException,
			MayamClientException {
		testProcessMessageValidMessageAndMedia(false, true, true);
	}

	@Test
	public void testProcessMessageValidMessageAndMediatitleDoesntExistMediaFirstMediaInvalid()
			throws FileNotFoundException, DatatypeConfigurationException,
			IOException, JAXBException, SAXException, InterruptedException,
			MayamClientException {
		testProcessMessageValidMessageAndMedia(false, true, false);
	}

	@Test
	public void testProcessMessageValidMessageAndMediatitleDoesntExistMessageFirstMediaValid()
			throws FileNotFoundException, DatatypeConfigurationException,
			IOException, JAXBException, SAXException, InterruptedException,
			MayamClientException {
		testProcessMessageValidMessageAndMedia(false, false, true);
	}

	@Test
	public void testProcessMessageValidMessageAndMediatitleDoesntExistMessageFirstMediaInValid()
			throws FileNotFoundException, DatatypeConfigurationException,
			IOException, JAXBException, SAXException, InterruptedException,
			MayamClientException {
		testProcessMessageValidMessageAndMedia(false, false, false);
	}

	public void testProcessFailsOnMayamExceptionCreatingOrUpdatingTitle(
			boolean titleExists) throws DatatypeConfigurationException,
			FileNotFoundException, IOException, MayamClientException,
			JAXBException, SAXException, InterruptedException {
		// prepare files
		material = MarketingMaterialTest.getMaterial(TITLE_ID);

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// prepare mocks
		when(validator.validateFile(materialXMLPath)).thenReturn(
				MessageValidationResult.IS_VALID);
		when(mayamClient.titleExists(TITLE_ID)).thenReturn(titleExists);
		when(mayamClient.updateTitle(argThat(titleIDMatcher))).thenReturn(
				MayamClientErrorCode.FAILURE);
		when(mayamClient.createTitle(argThat(titleIDMatcher))).thenReturn(
				MayamClientErrorCode.FAILURE);

		// add file to queue for processing
		filesPendingProcessingQueue.add(materialxml.getAbsolutePath());

		// wait for some time to allow processing to take place
		Thread.sleep(500l);

		// check message gets moved to failure folder
		assertFalse(materialxml.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml,
				new File(failurePath)).exists());
	}

	@Test
	public void testProcessingFailsWhenErrorQueryingTitleExistance()
			throws DatatypeConfigurationException, MayamClientException,
			InterruptedException, FileNotFoundException, JAXBException,
			SAXException {
		// prepare files
		material = MarketingMaterialTest.getMaterial(TITLE_ID);

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// prepare mocks
		when(validator.validateFile(materialXMLPath)).thenReturn(
				MessageValidationResult.IS_VALID);
		when(mayamClient.titleExists(TITLE_ID)).thenThrow(
				new MayamClientException(MayamClientErrorCode.FAILURE));
		// add file to queue for processing
		filesPendingProcessingQueue.add(materialxml.getAbsolutePath());

		// wait for some time to allow processing to take place
		Thread.sleep(500l);

		// check message gets moved to failure folder
		assertFalse(materialxml.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml,
				new File(failurePath)).exists());
	}

	@Test
	public void testProcessFailsOnMayamExceptionUpdatingTitle()
			throws FileNotFoundException, DatatypeConfigurationException,
			IOException, MayamClientException, JAXBException, SAXException,
			InterruptedException {
		testProcessFailsOnMayamExceptionCreatingOrUpdatingTitle(true);
	}

	@Test
	public void testProcessFailsOnMayamExceptionCreatingTitle()
			throws FileNotFoundException, DatatypeConfigurationException,
			IOException, MayamClientException, JAXBException, SAXException,
			InterruptedException {
		testProcessFailsOnMayamExceptionCreatingOrUpdatingTitle(false);

	}

	/**
	 * Test that a valid message is processed correctly
	 * 
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws SAXException
	 * @throws JAXBException
	 * @throws InterruptedException
	 * @throws MayamClientException
	 * 
	 */
	public void testProcessMessageValidMessageAndMedia(boolean titleExists,
			boolean mediaFirst, boolean validMedia)
			throws DatatypeConfigurationException, FileNotFoundException,
			IOException, JAXBException, SAXException, InterruptedException,
			MayamClientException {

		// prepare files
		TestUtil.writeBytesToFile(100, media);
		material = MarketingMaterialTest.getMaterial(TITLE_ID);

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// prepare mocks
		when(validator.validateFile(materialXMLPath)).thenReturn(
				MessageValidationResult.IS_VALID);
		when(mayamClient.titleExists(TITLE_ID)).thenReturn(titleExists);
		when(mayamClient.updateTitle(argThat(titleIDMatcher))).thenReturn(
				MayamClientErrorCode.SUCCESS);
		when(mayamClient.createTitle(argThat(titleIDMatcher))).thenReturn(
				MayamClientErrorCode.SUCCESS);

		if (mediaFirst) {
			when(matchMaker.matchMXF(media)).thenReturn(null);
			when(matchMaker.matchXML(argThat(matchEnvelopeByFile))).thenReturn(
					media.getAbsolutePath());

		} else {
			when(matchMaker.matchXML(argThat(matchEnvelopeByFile))).thenReturn(
					null);
			when(matchMaker.matchMXF(media)).thenReturn(
					new MaterialEnvelope(materialxml, material));
		}
		when(mediaCheck.mediaCheck(eq(media), argThat(matchEnvelopeByFile)))
				.thenReturn(validMedia);

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

		if (titleExists) {
			verify(mayamClient).updateTitle(argThat(titleIDMatcher));
		} else {
			verify(mayamClient).createTitle(argThat(titleIDMatcher));
		}

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

		if (validMedia) {
			// check there is a pending import on the queue
			assertTrue(pendingImportQueue.size() == 1);
			PendingImport pi = pendingImportQueue.take();
			assertTrue(pi.getMediaFile().equals(media));
			assertTrue(pi.getMaterialEnvelope().getFile().equals(materialxml));
		} else {
			// check message gets moved to failure folder and media gets moved to viz ardome emergency import folder
			assertFalse(materialxml.exists());
			assertFalse(media.exists());
			assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(
					materialxml, new File(failurePath)).exists());
			assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(media,
					new File(emergencyImportPath)).exists());

		}

	}

}
