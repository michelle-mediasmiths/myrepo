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

import com.mediasmiths.foxtel.placeholder.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class PurgeTitleTest extends PlaceHolderMessageValidatorTest {

	public PurgeTitleTest() throws JAXBException, SAXException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteTitleNotProtected() throws IOException, Exception {

		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		File temp = createTempXMLFile(pm,
				"validDeleteTitleMaterialNotProtected");

		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE))
				.thenReturn(false);

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

		assertEquals(
				MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED,
				validator.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testPurgeTitleProcessing() throws MessageProcessingFailedException{
		
		PlaceholderMessage pm =  buildDeleteTitleRequest(false, EXISTING_TITLE);
		
		PurgeTitle pt = (PurgeTitle) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.purgeTitle(pt)).thenReturn(MayamClientErrorCode.SUCCESS);
		
		//the call we are testing
		processor.processPlaceholderMesage(pm);
		
		//verify expected calls
		verify(mayamClient).purgeTitle(pt);
		
		
	}
	
	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testPurgeTitleProcessingFails() throws MessageProcessingFailedException{
		
	PlaceholderMessage pm =  buildDeleteTitleRequest(false, EXISTING_TITLE);
		
		PurgeTitle pt = (PurgeTitle) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.purgeTitle(pt)).thenReturn(MayamClientErrorCode.TITLE_UPDATE_FAILED);
		
		//the call we are testing
		processor.processPlaceholderMesage(pm);		
	}


	@Test(expected = MayamClientException.class)
	@Category(ValidationTests.class)
	public void testDeleteTitleRequestFail() throws IOException, Exception {
		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteTitleRequestFailure");

		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE))
				.thenThrow(
						new MayamClientException(MayamClientErrorCode.FAILURE));

		validator.validateFile(temp.getAbsolutePath());
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
