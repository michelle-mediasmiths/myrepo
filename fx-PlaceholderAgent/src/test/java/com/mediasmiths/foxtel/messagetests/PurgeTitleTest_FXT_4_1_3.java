package com.mediasmiths.foxtel.messagetests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class PurgeTitleTest_FXT_4_1_3 extends PlaceHolderMessageShortTest{
	private static Logger logger = Logger.getLogger(PurgeTitleTest_FXT_4_1_3.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	public PurgeTitleTest_FXT_4_1_3() throws JAXBException, SAXException, IOException {
		super();
	}

	@Test
	@Category (ProcessingTests.class)
	public void testPurgeTitleProcessing() throws MessageProcessingFailedException {
		
		logger.info("Processing test");
		PlaceholderMessage message = buildPurgeTitle(false, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		PurgeTitle pt = (PurgeTitle) message.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);
		
		when(mayamClient.purgeTitle(pt)).thenReturn(MayamClientErrorCode.SUCCESS);
		
		processor.processMessage(envelope);
		
		verify(mayamClient).purgeTitle(pt);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testPurgeTitleXSDInvalid_FXT_4_1_3_2() throws Exception {
		
		logger.info("Starting FXT 4.1.3.2 - Non XSD compliance");
		//File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		File temp = new File("/tmp/placeHolderTestData/NonXSDConformingFile__"+RandomStringUtils.randomAlphabetic(6)+ ".xml");

		IOUtils.write("InvalidPurgeTitle", new FileOutputStream(temp));
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.FAILS_XSD_CHECK ==validateFile)
			resultLogger.info("FXT 4.1.3.2 - Non XSD compliance --Passed");
		else
			resultLogger.info("FXT 4.1.3.2 - Non XSD compliance --Failed");
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleNotProtected_FXT_4_1_3_3_4_5_FXT_4_1_0_7() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.3.3/4/5 - XSD Compliance/ Valid PurgeTitle message/ Matching ID exists");
		logger.info("Starting FXT 4.1.0.7 – Valid PurgeTitle Message ");

		PlaceholderMessage message = buildPurgeTitle(false, EXISTING_TITLE);
		File temp = createTempXMLFile(message, "validPurgeTitleNotProtected");
		
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(false);
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.IS_VALID ==validateFile)
		{
			resultLogger.info("FXT 4.1.3.3/4/5 - XSD Compliance/ Valid PurgeTitle message/ Matching ID exists --Passed");
			resultLogger.info("FXT 4.1.0.7 – Valid PurgeTitle Message --Passed");
		}
		else
		{
			resultLogger.info("FXT 4.1.3.3/4/5 - XSD Compliance/ Valid PurgeTitle message/ Matching ID exists --Failed");
			resultLogger.info("FXT 4.1.0.7 – Valid PurgeTitle Message --Passed");
		}
		
		assertEquals(MessageValidationResult.IS_VALID, validateFile);	
		Util.deleteFiles(temp.getAbsolutePath());
	}
	
	@Test
	@Category (ValidationTests.class)
	public void testPurgeTitleDoesntExist_FXT_4_1_3_6() throws Exception {
		
		logger.info("Starting FXT 4.1.3.6 - No matching ID exists");
		
		PlaceholderMessage message = buildPurgeTitle(false, NOT_EXISTING_TITLE);
		File temp = createTempXMLFile (message, "purgeTitleDoesntExist");
		
		when(mayamClient.titleExists(NOT_EXISTING_TITLE)).thenReturn(false);
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.NO_EXISTING_TITLE_TO_PURGE ==validateFile)
			resultLogger.info("FXT 4.1.3.6 - No matching ID exists --Passed");
		else
			resultLogger.info("FXT 4.1.3.6 - No matching ID exists --Failed");
		
		assertEquals(MessageValidationResult.NO_EXISTING_TITLE_TO_PURGE, validateFile);
		Util.deleteFiles(temp.getAbsolutePath());
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleProtected_FXT_4_1_3_7() throws IOException, Exception {
		logger.info("Starting FXT 4.1.3.7 - Title is protected");
		PlaceholderMessage message = buildPurgeTitle(false, EXISTING_TITLE);
		File temp = createTempXMLFile(message, "validPurgeTitleProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(true);
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED ==validateFile)
		{
			resultLogger.info("FXT 4.1.3.7 - Title is protected --Passed");
		}
		else
		{
			resultLogger.info("FXT 4.1.3.7 - Title is protected --Failed");
		}

		assertEquals(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED, validateFile);
		Util.deleteFiles(temp.getAbsolutePath());
	}

	private PlaceholderMessage buildPurgeTitle(boolean b, String titleID) {
		
		PurgeTitle pt = new PurgeTitle();
		pt.setTitleID(titleID);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(pt);
		
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID(MESSAGE_ID);
		message.setSenderID(SENDER_ID);
		message.setActions(actions);
		
		return message;
	}
}
