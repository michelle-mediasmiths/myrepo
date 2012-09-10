package com.mediasmiths.foxtel.placeholder.requestValidation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidationResult;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

public class PurgeTitleTest extends PlaceHolderMessageValidatorTest {

	public PurgeTitleTest() throws JAXBException, SAXException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Test
	public void testDeleteTitleNotProtected() throws IOException, Exception {

		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		File temp = createTempXMLFile(pm,
				"validDeleteTitleMaterialNotProtected");

		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE))
				.thenReturn(false);

		assertEquals(PlaceHolderMessageValidationResult.IS_VALID,
				toTest.validateFile(temp.getAbsolutePath()));
	}

	@Test
	public void testDeleteTitleIsProected() throws IOException, Exception {
		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteTitleMaterialProtected");

		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE))
				.thenReturn(true);

		assertEquals(
				PlaceHolderMessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED,
				toTest.validateFile(temp.getAbsolutePath()));
	}

	@Test(expected = MayamClientException.class)
	public void testDeleteTitleRequestFail() throws IOException, Exception {
		PlaceholderMessage pm = buildDeleteTitleRequest(false, EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteTitleRequestFailure");

		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE))
				.thenThrow(
						new MayamClientException(MayamClientErrorCode.FAILURE));

		toTest.validateFile(temp.getAbsolutePath());
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
