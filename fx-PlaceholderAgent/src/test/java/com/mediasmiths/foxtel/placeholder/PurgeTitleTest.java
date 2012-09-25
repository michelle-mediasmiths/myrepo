package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class PurgeTitleTest extends PlaceHolderMessageShortTest {

	public PurgeTitleTest() throws JAXBException, SAXException, IOException {
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

		assertEquals(MessageValidationResult.IS_VALID,
				validator.validateFile(temp.getAbsolutePath()));
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleIsProected() throws IOException, Exception {
		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteTitleMaterialProtected");

		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE))
				.thenReturn(true);

		assertEquals(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED,
				validator.validateFile(temp.getAbsolutePath()));
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
	}

	private PlaceholderMessage buildDeleteTitleRequest(boolean b, String titleID) {

		PurgeTitle pt = new PurgeTitle();
		pt.setTitleID(titleID);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				pt);

		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(MESSAGE_ID);
		pm.setSenderID(SENDER_ID);
		pm.setActions(actions);
		return pm;

	}

}
