package com.mediasmiths.foxtel.mpa.processing;

import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResultPackage;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.ProgrammeMaterialTest;
import com.mediasmiths.foxtel.mpa.ResultLogger;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
		
		MessageValidationResultPackage<Material> failedToUnmarshallResult = new MessageValidationResultPackage<>(new PickupPackage("xml"), MessageValidationResult.FAILED_TO_UNMARSHALL);
		
		// prepare mocks
		when(validator.validatePickupPackage(any(PickupPackage.class))).thenReturn(failedToUnmarshallResult);
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// wait for some time to allow processing to take place
		Thread.sleep(500l);

		// check message gets moved to failure folder
		assertFalse(materialxml.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml,
				new File(failurePath)).exists());
	}

	@Test
	@Ignore
	public void testValidMessageValidMediaMediaFirst_FXT_4_6_2_1()
			throws FileNotFoundException, InterruptedException, IOException,
			DatatypeConfigurationException, JAXBException, SAXException,
			MayamClientException {
		
		String testName="FXT 4.6.2.2_1  -  Title /Material/Package metadata is updated in Viz Ardome /Media matched with placeholder(Part2)";
		logger.info("Starting" +testName);
		
		testMessageProcesses(testName);
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
	private void testMessageProcesses(String testName)
			throws InterruptedException, FileNotFoundException, IOException,
			DatatypeConfigurationException, JAXBException, SAXException,
			MayamClientException {
		// prepare files
		material = ProgrammeMaterialTest.getMaterialWithPackages(TITLE_ID,
				MATERIAL_ID, Arrays.asList(PACKAGE_IDS));

		materialXMLPath = materialxml.getAbsolutePath();

		// prepare mocks
		MessageValidationResultPackage<Material> validresult = new MessageValidationResultPackage<>(new PickupPackage("xml"), MessageValidationResult.IS_VALID);
		when(validator.validatePickupPackage(any(PickupPackage.class))).thenReturn(validresult);
		when(mayamClient.updateTitle((Title) argThat(titleIDMatcher)))
				.thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.updateMaterial(argThat(materialIDMatcher),any(Details.class),any(Material.Title.class)))
				.thenReturn(false);
		when(mayamClient.materialHasPassedPreview(anyString())).thenReturn(false);
		when(mayamClient.getLastDeliveryVersionForMaterial(anyString())).thenReturn(-1);
		
		// wait for some time to allow processing to take place
		Thread.sleep(500l);

		// verfiy mocks
		verify(validator).validatePickupPackage(any(PickupPackage.class));
		verify(mayamClient).updateTitle(argThat(titleIDMatcher));
		verify(mayamClient).updateMaterial(argThat(materialIDMatcher),any(Details.class),any(Material.Title.class));

		// check the files pending processing queue has been consumed
		assertTrue(filesPendingProcessingQueue.size() == 0);

			
			// check there is a pending import on the queue
			assertTrue(pendingImportQueue.size() == 1);
			PendingImport pi = pendingImportQueue.take();
			
			Boolean materialTest=pi.getMaterialEnvelope().getPickupPackage().getPickUp("xml").equals(materialxml);
			assertTrue(materialTest);
	}
	
	@Test
	public void testMayamClientFailureOnUpdateTitleResultsInProcessingFailure() throws DatatypeConfigurationException, FileNotFoundException, JAXBException, SAXException, InterruptedException{
		// prepare files
		material = ProgrammeMaterialTest.getMaterialWithPackages(TITLE_ID,
				MATERIAL_ID, Arrays.asList(PACKAGE_IDS));

		materialXMLPath = materialxml.getAbsolutePath();
		
		MessageValidationResultPackage<Material> validresult = new MessageValidationResultPackage<>(new PickupPackage("xml"), MessageValidationResult.IS_VALID);
		
		// prepare mocks
		when(validator.validatePickupPackage(any(PickupPackage.class))).thenReturn(validresult);
		when(mayamClient.updateTitle((Title) argThat(titleIDMatcher)))
				.thenReturn(MayamClientErrorCode.FAILURE);
		
		TestUtil.writeMaterialToFile(material, materialXMLPath);
		
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
	public void testMayamClientFailureOnUpdateProgrammeMaterialResultsInProcessingFailure() throws DatatypeConfigurationException, FileNotFoundException, JAXBException, SAXException, InterruptedException, MayamClientException{
		// prepare files
		material = ProgrammeMaterialTest.getMaterialWithPackages(TITLE_ID,
				MATERIAL_ID, Arrays.asList(PACKAGE_IDS));

		materialXMLPath = materialxml.getAbsolutePath();
		
		
		// prepare mocks
		MessageValidationResultPackage<Material> validresult = new MessageValidationResultPackage<>(new PickupPackage("xml"), MessageValidationResult.IS_VALID);
		when(validator.validatePickupPackage(any(PickupPackage.class))).thenReturn(validresult);
		when(mayamClient.updateTitle((Title) argThat(titleIDMatcher)))
				.thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.updateMaterial(argThat(materialIDMatcher),any(Details.class),any(Material.Title.class)))
		.thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
		
		TestUtil.writeMaterialToFile(material, materialXMLPath);
		
		// wait for some time to allow processing to take place
		Thread.sleep(1000l);
		
		//check queu is now empty
		assertTrue(pendingImportQueue.size() == 0);
		
		// check message gets moved to failure folder
		assertFalse(materialxml.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(
				materialxml, new File(failurePath)).exists());
	}
		
}
