package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.messagetests.ResultLogger;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PurgeTitleTest_FXT_4_1_24_25_26 extends PlaceHolderMessageShortTest
{
	private static Logger logger = Logger.getLogger(PurgeTitleTest_FXT_4_1_24_25_26.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);
	
	public PurgeTitleTest_FXT_4_1_24_25_26() throws JAXBException, SAXException, IOException {
		super();
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleNotProtected() throws IOException, Exception {


		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (pm,
				"validDeleteTitleMaterialNotProtected");

		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE))
				.thenReturn(false);
		
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		validator.validatePurgeTitle((PurgeTitle) pm.getActions()
		                                            .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
		                                            .get(0));
		Util.deleteFiles(pp);
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleIsProected_FXT_4_1_24_25_26() throws IOException, Exception {
		logger.info("Starting FXT 4.1.24/25/26 ");

		PlaceholderMessage pm = buildDeleteTitleRequest(false, PROTECTED_TITLE);
		PickupPackage pp = createTempXMLFile (pm, "validDeleteTitleMaterialProtected");

		when(mayamClient.isTitleOrDescendentsProtected(PROTECTED_TITLE))
				.thenReturn(true);

		
		try
		{
			validator.validatePurgeTitle((PurgeTitle) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
			resultLogger.info("FXT 4.1.24/25/26  --Failed");

			fail();
		}
		catch(MessageValidationException r)
		{
			assertEquals(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED, r.result);
			resultLogger.info("FXT 4.1.24/25/26 --Passed");
		}



		Util.deleteFiles(pp);
	}

	@Test
	@Category(ProcessingTests.class)
	public void testPurgeTitleProcessing() throws MessageProcessingFailedException, MessageValidationException, MayamClientException
	{

		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(
				new PickupPackage("xml"), pm);

		PurgeTitle pt = (PurgeTitle) pm.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);

		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		// prepare mock mayam client
		when(mayamClient.purgeTitle(pt)).thenReturn(
				MayamClientErrorCode.SUCCESS);

		// the call we are testing
		processor.purgeTitle((PurgeTitle) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));

		// verify expected calls
		verify(mayamClient).purgeTitle(pt);

	}

	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testPurgeTitleProcessingFails() throws MessageProcessingFailedException, MessageValidationException, MayamClientException
	{

		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(
				new PickupPackage("xml"), pm);

		PurgeTitle pt = (PurgeTitle) pm.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);

		// prepare mock mayam client
		when(mayamClient.purgeTitle(pt)).thenReturn(
				MayamClientErrorCode.TITLE_UPDATE_FAILED);

		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		// the call we are testing
		processor.purgeTitle((PurgeTitle) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleRequestFail() throws IOException, Exception {
		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (pm, "validDeleteTitleRequestFailure");

		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE))
				.thenThrow(
						new MayamClientException(MayamClientErrorCode.FAILURE));

		try
		{
			validator.validatePurgeTitle((PurgeTitle)pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
			fail();
		}
		catch (MessageValidationException e)
		{
			assertEquals(MessageValidationResult.MAYAM_CLIENT_ERROR,
			             e.result);
		}
		// try to call validation, expect a mayam client error

		Util.deleteFiles(pp);
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
