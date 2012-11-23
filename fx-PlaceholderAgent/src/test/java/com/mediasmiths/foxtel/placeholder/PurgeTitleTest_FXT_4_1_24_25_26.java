package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

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
import com.mediasmiths.foxtel.messagetests.PurgeTitleTest_FXT_4_1_3;
import com.mediasmiths.foxtel.messagetests.ResultLogger;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class PurgeTitleTest_FXT_4_1_24_25_26 extends PlaceHolderMessageShortTest {
	private static Logger logger = Logger.getLogger(PurgeTitleTest_FXT_4_1_24_25_26.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);
	
	public PurgeTitleTest_FXT_4_1_24_25_26() throws JAXBException, SAXException, IOException {
		super();
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleNotProtected() throws IOException, Exception {


		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		File temp = createTempXMLFile(pm,
				"validDeleteTitleMaterialNotProtected");

		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE))
				.thenReturn(false);
		
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		assertEquals(MessageValidationResult.IS_VALID,validateFile);
		Util.deleteFiles(temp.getAbsolutePath());
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleIsProected_FXT_4_1_24_25_26() throws IOException, Exception {
		logger.info("Starting FXT 4.1.24/25/26 ");

		PlaceholderMessage pm = buildDeleteTitleRequest(false, PROTECTED_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteTitleMaterialProtected");

		when(mayamClient.isTitleOrDescendentsProtected(PROTECTED_TITLE))
				.thenReturn(true);

		
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED ==validateFile)
		{
		resultLogger.info("FXT 4.1.24/25/26 --Passed");
		}
		else
		{
		resultLogger.info("FXT 4.1.24/25/26  --Failed");
		}
		assertEquals(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED,validateFile);

		Util.deleteFiles(temp.getAbsolutePath());
	}

	@Test
	@Category(ProcessingTests.class)
	public void testPurgeTitleProcessing()
			throws MessageProcessingFailedException {

		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(
				new File("/dev/null"), pm);

		PurgeTitle pt = (PurgeTitle) pm.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);

		// prepare mock mayam client
		when(mayamClient.purgeTitle(pt)).thenReturn(
				MayamClientErrorCode.SUCCESS);

		// the call we are testing
		processor.processMessage(envelope);

		// verify expected calls
		verify(mayamClient).purgeTitle(pt);

	}

	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testPurgeTitleProcessingFails()
			throws MessageProcessingFailedException {

		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(
				new File("/dev/null"), pm);

		PurgeTitle pt = (PurgeTitle) pm.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);

		// prepare mock mayam client
		when(mayamClient.purgeTitle(pt)).thenReturn(
				MayamClientErrorCode.TITLE_UPDATE_FAILED);

		// the call we are testing
		processor.processMessage(envelope);
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleRequestFail() throws IOException, Exception {
		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteTitleRequestFailure");

		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE))
				.thenThrow(
						new MayamClientException(MayamClientErrorCode.FAILURE));

		// try to call validation, expect a mayam client error
		assertEquals(MessageValidationResult.MAYAM_CLIENT_ERROR,
				validator.validateFile(temp.getAbsolutePath()));
		Util.deleteFiles(temp.getAbsolutePath());
	}

	private PlaceholderMessage buildDeleteTitleRequest(boolean b, String titleID) {

		PurgeTitle pt = new PurgeTitle();
		pt.setTitleID(titleID);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				pt);

		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(createMessageID());
		pm.setSenderID(createSenderID());
		pm.setActions(actions);
		return pm;

	}

}
