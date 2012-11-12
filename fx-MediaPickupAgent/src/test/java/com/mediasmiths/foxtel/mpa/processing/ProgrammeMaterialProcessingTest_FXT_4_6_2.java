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
import java.util.Arrays;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mockito.InOrder;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.mpa.MaterialEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.ProgrammeMaterialTest;
import com.mediasmiths.foxtel.mpa.ResultLogger;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class ProgrammeMaterialProcessingTest_FXT_4_6_2 extends MaterialProcessingTest {
	
	private static Logger logger = Logger.getLogger(ProgrammeMaterialProcessingTest_FXT_4_6_2.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);
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
		material = ProgrammeMaterialTest.getMaterialWithPackages(TITLE_ID,
				MATERIAL_ID, Arrays.asList(PACKAGE_IDS));

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
	}

	@Test
	public void testValidMessageValidMediaMediaFirst_FXT_4_6_2_1()
			throws FileNotFoundException, InterruptedException, IOException,
			DatatypeConfigurationException, JAXBException, SAXException,
			MayamClientException {
		
		String testName="FXT 4.6.2.2_1  -  Title /Material/Package metadata is updated in Viz Ardome /Media matched with placeholder(Part2)";
		logger.info("Starting" +testName);
		
		testMessageProcesses(true,true, testName);
	}

	@Test
	public void testValidMessageValidMediaMessageFirst_FXT_4_6_2_1()
			throws FileNotFoundException, InterruptedException, IOException,
			DatatypeConfigurationException, JAXBException, SAXException,
			MayamClientException {
		
		String testName="FXT 4.6.2.2_1  -  Title /Material/Package metadata is updated in Viz Ardome/Media matched with placeholder(Part2)";
		logger.info("Starting" +testName);
		
		
		testMessageProcesses(true,false, testName);
	}

	@Test
	public void testValidMessageInValidMediaMediaFirst()
			throws FileNotFoundException, InterruptedException, IOException,
			DatatypeConfigurationException, JAXBException, SAXException,
			MayamClientException {
		testMessageProcesses(false, true, null);
	}

	@Test
	public void testValidMessageInValidMediaMessageFirst()
			throws FileNotFoundException, InterruptedException, IOException,
			DatatypeConfigurationException, JAXBException, SAXException,
			MayamClientException {
		testMessageProcesses(false, false, null);
	}

	/**
	 * Test the handling of programme material messages
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws DatatypeConfigurationException
	 * @throws SAXException
	 * @throws JAXBException
	 * @throws MayamClientException
	 */
	private void testMessageProcesses(boolean mediaValid, boolean mediaFirst, String testName)
			throws InterruptedException, FileNotFoundException, IOException,
			DatatypeConfigurationException, JAXBException, SAXException,
			MayamClientException {
		// prepare files
		TestUtil.writeBytesToFile(100, media);
		material = ProgrammeMaterialTest.getMaterialWithPackages(TITLE_ID,
				MATERIAL_ID, Arrays.asList(PACKAGE_IDS));

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// prepare mocks
		when(validator.validateFile(materialXMLPath)).thenReturn(
				MessageValidationResult.IS_VALID);
		when(mayamClient.updateTitle((Title) argThat(titleIDMatcher)))
				.thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.updateMaterial(argThat(materialIDMatcher)))
				.thenReturn(MayamClientErrorCode.SUCCESS);

		when(mayamClient.updatePackage(argThat(matchByPackageID1))).thenReturn(
				MayamClientErrorCode.SUCCESS);
		when(mayamClient.updatePackage(argThat(matchByPackageID2))).thenReturn(
				MayamClientErrorCode.SUCCESS);
		when(mayamClient.updatePackage(argThat(matchByPackageID3))).thenReturn(
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
				.thenReturn(mediaValid);

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

		verify(mayamClient).updatePackage(argThat(matchByPackageID1));
		verify(mayamClient).updatePackage(argThat(matchByPackageID2));
		verify(mayamClient).updatePackage(argThat(matchByPackageID3));

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

		if (mediaValid) {
			
			// check there is a pending import on the queue
			assertTrue(pendingImportQueue.size() == 1);
			PendingImport pi = pendingImportQueue.take();
			Boolean mediaTest =pi.getMediaFile().equals(media);
			assertTrue(mediaTest);
			
			Boolean materialTest=pi.getMaterialEnvelope().getFile().equals(materialxml);
			assertTrue(materialTest);
			
			if (testName !=null)
			{
				if(materialTest && mediaTest)
				{
            		resultLogger.info(testName+ "-- Passed");
				}
				else 
				{
            		resultLogger.info(testName+"-- FAILED");
				}
			}

		} else {

			// check there is not pending import on the queue
			assertTrue(pendingImportQueue.size() == 0);
			// check message and material gets moved to failure folder
			Boolean materialExist= materialxml.exists();
			assertFalse(materialExist);
			Boolean mediaExists= media.exists();
			assertFalse(mediaExists);
			
			Boolean failurePathTest=TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml, new File(failurePath)).exists();
			assertTrue(failurePathTest);
			
			Boolean importPathTest=TestUtil.getPathToThisFileIfItWasInThisFolder(media,new File(emergencyImportPath)).exists();
			assertTrue(importPathTest);
			
			if (testName !=null)
			{
				if(!materialExist && !mediaExists && failurePathTest && importPathTest)
				{
            		resultLogger.info(testName+ "-- Passed");
				}
				else 
				{
            		resultLogger.info(testName+"-- FAILED");
				}
			}
			
			
		}

	}
	
	@Test
	public void testMayamClientFailureOnUpdateTitleResultsInProcessingFailure() throws DatatypeConfigurationException, FileNotFoundException, JAXBException, SAXException, InterruptedException{
		// prepare files
		material = ProgrammeMaterialTest.getMaterialWithPackages(TITLE_ID,
				MATERIAL_ID, Arrays.asList(PACKAGE_IDS));

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// prepare mocks
		when(validator.validateFile(materialXMLPath)).thenReturn(
				MessageValidationResult.IS_VALID);
		when(mayamClient.updateTitle((Title) argThat(titleIDMatcher)))
				.thenReturn(MayamClientErrorCode.FAILURE);
		
		//queue file for processing
		filesPendingProcessingQueue.add(materialxml.getAbsolutePath());
		
		// wait for some time to allow processing to take place
		Thread.sleep(500l);
		
		//check queu is now empty
		assertTrue(pendingImportQueue.size() == 0);
		
		// check message gets moved to failure folder
		assertFalse(materialxml.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(
				materialxml, new File(failurePath)).exists());
	}
	
	@Test
	public void testMayamClientFailureOnUpdateProgrammeMaterialResultsInProcessingFailure() throws DatatypeConfigurationException, FileNotFoundException, JAXBException, SAXException, InterruptedException{
		// prepare files
		material = ProgrammeMaterialTest.getMaterialWithPackages(TITLE_ID,
				MATERIAL_ID, Arrays.asList(PACKAGE_IDS));

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// prepare mocks
		when(validator.validateFile(materialXMLPath)).thenReturn(
				MessageValidationResult.IS_VALID);
		when(mayamClient.updateTitle((Title) argThat(titleIDMatcher)))
				.thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.updateMaterial(argThat(materialIDMatcher)))
		.thenReturn(MayamClientErrorCode.FAILURE);
				
		//queue file for processing
		filesPendingProcessingQueue.add(materialxml.getAbsolutePath());
		
		// wait for some time to allow processing to take place
		Thread.sleep(500l);
		
		//check queu is now empty
		assertTrue(pendingImportQueue.size() == 0);
		
		// check message gets moved to failure folder
		assertFalse(materialxml.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(
				materialxml, new File(failurePath)).exists());
	}
	
	@Test
	public void testMayamClientFailureOnUpdatePackageResultsInProcessingFailure() throws DatatypeConfigurationException, FileNotFoundException, JAXBException, SAXException, InterruptedException{
		// prepare files
		material = ProgrammeMaterialTest.getMaterialWithPackages(TITLE_ID,
				MATERIAL_ID, Arrays.asList(PACKAGE_IDS));

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// prepare mocks
		when(validator.validateFile(materialXMLPath)).thenReturn(
				MessageValidationResult.IS_VALID);
		when(mayamClient.updateTitle((Title) argThat(titleIDMatcher)))
				.thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.updateMaterial(argThat(materialIDMatcher)))
		.thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.updatePackage(argThat(matchByPackageID1))).thenReturn(
				MayamClientErrorCode.FAILURE);
				
		//queue file for processing
		filesPendingProcessingQueue.add(materialxml.getAbsolutePath());
		
		// wait for some time to allow processing to take place
		Thread.sleep(500l);
		
		//check queu is now empty
		assertTrue(pendingImportQueue.size() == 0);
		
		// check message gets moved to failure folder
		assertFalse(materialxml.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(
				materialxml, new File(failurePath)).exists());
	}
	
	
}