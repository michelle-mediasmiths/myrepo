package com.mediasmiths.foxtel.messagetests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.junit.categories.ValidationTests;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class PurgeTitleTest extends PlaceHolderMessageShortTest{
	
	public PurgeTitleTest() throws JAXBException, SAXException, IOException {
		super();
	}

	@Test
	@Category (ProcessingTests.class)
	public void testPurgeTitleProcessing() throws MessageProcessingFailedException {
		
		System.out.println("Processing test");
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
	public void testPurgeTitleXSDInvalid() throws Exception {
		
		System.out.println("FXT 4.1.3.2 - Non XSD compliance");
		File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		IOUtils.write("InvalidPurgeTitle", new FileOutputStream(temp));
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleNotProtected() throws IOException, Exception {
		
		System.out.println("FXT 4.1.3.3/4/5 - XSD Compliance/ Valid PurgeTitle message/ Matching ID exists");
		PlaceholderMessage message = buildPurgeTitle(false, EXISTING_TITLE);
		File temp = createTempXMLFile(message, "validPurgeTitleNotProtected");
		
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(false);
		
		assertEquals(MessageValidationResult.IS_VALID, validator.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	@Category (ValidationTests.class)
	public void testPurgeTitleDoesntExist() throws Exception {
		
		System.out.println("FXT 4.1.3.6 - No matching ID exists");
		
		PlaceholderMessage message = buildPurgeTitle(false, NOT_EXISTING_TITLE);
		File temp = createTempXMLFile (message, "purgeTitleDoesntExist");
		
		when(mayamClient.titleExists(NOT_EXISTING_TITLE)).thenReturn(false);
		
		assertEquals(MessageValidationResult.NO_EXISTING_TITLE_TO_PURGE, validator.validateFile(temp.getAbsolutePath()));
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleProtected() throws IOException, Exception {
		
		System.out.println("FXT 4.1.3.7 - Title is protected");
		PlaceholderMessage message = buildPurgeTitle(false, EXISTING_TITLE);
		File temp = createTempXMLFile(message, "validPurgeTitleProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(true);
		
		assertEquals(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED,
				validator.validateFile(temp.getAbsolutePath()));
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
