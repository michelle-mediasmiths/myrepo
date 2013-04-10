package com.mediasmiths.foxtel.mpa.processing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResultPackage;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.mpa.MarketingMaterialTest;
import com.mediasmiths.foxtel.mpa.ResultLogger;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class MarketingMaterialProcessingTest_FXT_4_6_3 extends MaterialProcessingTest
{

	private static Logger logger = Logger.getLogger(MarketingMaterialProcessingTest_FXT_4_6_3.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	@Test
	public void testProcessMessageValidMessageAndMediaTitleExists_FXT_4_6_3_2_3()
			throws FileNotFoundException,
			DatatypeConfigurationException,
			IOException,
			JAXBException,
			SAXException,
			InterruptedException,
			MayamClientException
	{
		String testName = "FXT 4.6.3.2/3  - Marketing material message references existing title/Mayam ingest task for item is updated ";
		logger.info("Starting" + testName);

		testProcessMessageValidMessageAndMedia(true, testName);
	}

	@Test
	public void testProcessMessageValidMessageAndMediatitleDoesntExist_FXT_4_6_3_1()
			throws FileNotFoundException,
			DatatypeConfigurationException,
			IOException,
			JAXBException,
			SAXException,
			InterruptedException,
			MayamClientException
	{

		String testName = "FXT 4.6.3.1  - Marketing material message references non existing title";
		logger.info("Starting" + testName);

		testProcessMessageValidMessageAndMedia(false, testName);
	}

	public void testProcessFailsOnMayamExceptionCreatingOrUpdatingTitle(boolean titleExists)
			throws DatatypeConfigurationException,
			FileNotFoundException,
			IOException,
			MayamClientException,
			JAXBException,
			SAXException,
			InterruptedException
	{
		// prepare files
		material = MarketingMaterialTest.getMaterial(TITLE_ID);

		materialXMLPath = materialxml.getAbsolutePath();

		// prepare mocks
		MessageValidationResultPackage<Material> validresult = new MessageValidationResultPackage<>(
				new PickupPackage("xml"),
				MessageValidationResult.IS_VALID);
		when(validator.validatePickupPackage(any(PickupPackage.class))).thenReturn(validresult);
		when(mayamClient.titleExists(TITLE_ID)).thenReturn(titleExists);
		when(mayamClient.updateTitle(argThat(titleIDMatcher))).thenReturn(MayamClientErrorCode.FAILURE);
		when(mayamClient.createTitle(argThat(titleIDMatcher))).thenReturn(MayamClientErrorCode.FAILURE);

		// write file
		TestUtil.writeMaterialToFile(material, materialXMLPath);
		
		// wait for some time to allow processing to take place
		Thread.sleep(500l);

		// check message gets moved to failure folder
		assertFalse(materialxml.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml, new File(failurePath)).exists());
	}

	@Test
	public void testProcessingFailsWhenErrorQueryingTitleExistance()
			throws DatatypeConfigurationException,
			MayamClientException,
			InterruptedException,
			FileNotFoundException,
			JAXBException,
			SAXException
	{
		// prepare files
		material = MarketingMaterialTest.getMaterial(TITLE_ID);

		materialXMLPath = materialxml.getAbsolutePath();
	

		// prepare mocks
		MessageValidationResultPackage<Material> validresult = new MessageValidationResultPackage<>(
				new PickupPackage("xml"),
				MessageValidationResult.IS_VALID);
		when(validator.validatePickupPackage(any(PickupPackage.class))).thenReturn(validresult);
		when(mayamClient.titleExists(TITLE_ID)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
		// write file
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// wait for some time to allow processing to take place
		Thread.sleep(500l);

		// check message gets moved to failure folder
		assertFalse(materialxml.exists());
		assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml, new File(failurePath)).exists());
	}

	@Test
	public void testProcessFailsOnMayamExceptionUpdatingTitle()
			throws FileNotFoundException,
			DatatypeConfigurationException,
			IOException,
			MayamClientException,
			JAXBException,
			SAXException,
			InterruptedException
	{
		testProcessFailsOnMayamExceptionCreatingOrUpdatingTitle(true);
	}

	@Test
	public void testProcessFailsOnMayamExceptionCreatingTitle()
			throws FileNotFoundException,
			DatatypeConfigurationException,
			IOException,
			MayamClientException,
			JAXBException,
			SAXException,
			InterruptedException
	{
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
	public void testProcessMessageValidMessageAndMedia(boolean titleExists, String testName)
			throws DatatypeConfigurationException,
			FileNotFoundException,
			IOException,
			JAXBException,
			SAXException,
			InterruptedException,
			MayamClientException
	{

		// prepare files
		TestUtil.writeBytesToFile(100, media);
		material = MarketingMaterialTest.getMaterial(TITLE_ID);

		materialXMLPath = materialxml.getAbsolutePath();
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		MessageValidationResultPackage<Material> validresult = new MessageValidationResultPackage<>(
				new PickupPackage("xml"),
				MessageValidationResult.IS_VALID);
		// prepare mocks
		when(validator.validatePickupPackage(any(PickupPackage.class))).thenReturn(validresult);
		when(mayamClient.titleExists(TITLE_ID)).thenReturn(titleExists);
		when(mayamClient.updateTitle(argThat(titleIDMatcher))).thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.createTitle(argThat(titleIDMatcher))).thenReturn(MayamClientErrorCode.SUCCESS);

		// write file
		TestUtil.writeMaterialToFile(material, materialXMLPath);

		// wait for some time to allow processing to take place
		Thread.sleep(500l);

		if (titleExists)
		{
			verify(mayamClient).updateTitle(argThat(titleIDMatcher));
		}
		else
		{
			verify(mayamClient).createTitle(argThat(titleIDMatcher));
		}

		// check the files pending processing queue has been consumed
		assertTrue(filesPendingProcessingQueue.size() == 0);

		// check there is a pending import on the queue
	
		//TODO : check files go to right place
	}

}