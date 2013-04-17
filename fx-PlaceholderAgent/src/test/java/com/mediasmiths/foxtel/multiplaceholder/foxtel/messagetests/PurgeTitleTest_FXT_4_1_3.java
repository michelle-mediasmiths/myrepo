package com.mediasmiths.foxtel.multiplaceholder.foxtel.messagetests;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResultPackage;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PurgeTitleTest_FXT_4_1_3 extends PlaceHolderMessageShortTest
{
	private static Logger logger = Logger.getLogger(PurgeTitleTest_FXT_4_1_3.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	public PurgeTitleTest_FXT_4_1_3() throws JAXBException, SAXException, IOException
	{
		super();
	}

	@Test
	@Category(ProcessingTests.class)
	public void testPurgeTitleProcessing() throws MessageProcessingFailedException, Exception
	{

		logger.info("Processing test");
		PlaceholderMessage message = buildPurgeTitle(false, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), message);

		PurgeTitle pt = (PurgeTitle) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);

		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		when(mayamClient.purgeTitle(pt)).thenReturn(MayamClientErrorCode.SUCCESS);

		processor.processMessage(envelope);

		verify(mayamClient).purgeTitle(pt);
	}

	@Test
	@Category(ValidationTests.class)
	public void testPurgeTitleXSDInvalid_FXT_4_1_3_2() throws Exception
	{

		logger.info("Starting FXT 4.1.3.2 - Non XSD compliance");
		// File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		File temp = new File("/tmp/placeHolderTestData/NonXSDConformingFile__" + RandomStringUtils.randomAlphabetic(6) + ".xml");
		PickupPackage pp = new PickupPackage("xml");
		pp.addPickUp(temp);
		IOUtils.write("InvalidPurgeTitle", new FileOutputStream(temp));
		MessageValidationResultPackage<PlaceholderMessage> validationResult = validator.validatePickupPackage(pp);
		if (MessageValidationResult.FAILS_XSD_CHECK == validationResult.getResult())
			resultLogger.info("FXT 4.1.3.2 - Non XSD compliance --Passed");
		else
			resultLogger.info("FXT 4.1.3.2 - Non XSD compliance --Failed");

		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validationResult.getResult());
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleNotProtected_FXT_4_1_3_3_4_5_FXT_4_1_0_7() throws IOException, Exception
	{

		logger.info("Starting FXT 4.1.3.3/4/5 - XSD Compliance/ Valid PurgeTitle message/ Matching ID exists");
		logger.info("Starting FXT 4.1.0.7 ��� Valid PurgeTitle Message ");

		PlaceholderMessage message = buildPurgeTitle(false, EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile(message, "validPurgeTitleNotProtected");

		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(false);

		MessageValidationResultPackage<PlaceholderMessage> validationResult = validator.validatePickupPackage(pp);
		if (MessageValidationResult.IS_VALID == validationResult.getResult())
		{
			resultLogger.info("FXT 4.1.3.3/4/5 - XSD Compliance/ Valid PurgeTitle message/ Matching ID exists --Passed");
			resultLogger.info("FXT 4.1.0.7 ��� Valid PurgeTitle Message --Passed");
		}
		else
		{
			resultLogger.info("FXT 4.1.3.3/4/5 - XSD Compliance/ Valid PurgeTitle message/ Matching ID exists --Failed");
			resultLogger.info("FXT 4.1.0.7 ��� Valid PurgeTitle Message --Passed");
		}

		assertEquals(MessageValidationResult.IS_VALID, validationResult.getResult());
		Util.deleteFiles(pp);
	}

	@Test
	@Category(ValidationTests.class)
	public void testPurgeTitleDoesntExist_FXT_4_1_3_6() throws Exception
	{

		logger.info("Starting FXT 4.1.3.6 - No matching ID exists");

		PlaceholderMessage message = buildPurgeTitle(false, NOT_EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (message, "purgeTitleDoesntExist");

		when(mayamClient.titleExists(NOT_EXISTING_TITLE)).thenReturn(false);

		try
		{
			validator.validatePurgeTitle((PurgeTitle) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
			resultLogger.info("FXT 4.1.3.6 - No matching ID exists --Failed");
            fail();
		}
		catch (MessageValidationException e)
		{
			resultLogger.info("FXT 4.1.3.6 - No matching ID exists --Passed");

			assertEquals(MessageValidationResult.NO_EXISTING_TITLE_TO_PURGE, e.result);
		}
		Util.deleteFiles(pp);
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleProtected_FXT_4_1_3_7() throws IOException, Exception
	{
		logger.info("Starting FXT 4.1.3.7 - Title is protected");
		PlaceholderMessage message = buildPurgeTitle(false, PROTECTED_TITLE);
		PickupPackage pp = createTempXMLFile(message, "validPurgeTitleProtected");

		when(mayamClient.isTitleOrDescendentsProtected(PROTECTED_TITLE)).thenReturn(true);


		try
		{
			validator.validatePurgeTitle((PurgeTitle) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
			resultLogger.info("FXT 4.1.3.7 - Title is protected --Failed");
			fail();
		}
		catch (MessageValidationException e)
		{
			resultLogger.info("FXT 4.1.3.7 - Title is protected --Passed");

			assertEquals(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED, e.result);
		}

		Util.deleteFiles(pp);
	}

	private PlaceholderMessage buildPurgeTitle(boolean b, String titleID)
	{

		PurgeTitle pt = new PurgeTitle();
		pt.setTitleID(titleID);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(pt);

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID(createMessageID());
		message.setSenderID(createSenderID());
		message.setActions(actions);

		return message;
	}
}
