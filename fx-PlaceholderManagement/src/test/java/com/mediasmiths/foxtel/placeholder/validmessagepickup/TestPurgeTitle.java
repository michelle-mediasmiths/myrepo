package com.mediasmiths.foxtel.placeholder.validmessagepickup;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.RandomStringUtils;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class TestPurgeTitle extends ValidMessagePickTest {

	public TestPurgeTitle() throws JAXBException, SAXException {
		super();
	}

	protected PlaceholderMessage generatePlaceholderMessage() {

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID(RandomStringUtils.randomAlphabetic(6));
		message.setSenderID(RandomStringUtils.randomAlphabetic(6));

		PurgeTitle purgeTitle = generatePurgeTitle();

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				purgeTitle);
		message.setActions(actions);

		return message;
	}

	private PurgeTitle generatePurgeTitle() {

		PurgeTitle purgeTitle = new PurgeTitle();
		HelperMethods method = new HelperMethods();
		String titleId = method.validTitleId();
		purgeTitle.setTitleID(titleId);

		return purgeTitle;
	}

	@Override
	protected void mockValidCalls(PlaceholderMessage mesage) throws Exception {
		when(mayamClient.purgeTitle((PurgeTitle) anyObject())).thenReturn(
				MayamClientErrorCode.SUCCESS);
	}

	@Override
	protected void verifyValidCalls(PlaceholderMessage message) {
		verify(mayamClient).purgeTitle((PurgeTitle) anyObject());
	}

	@Override
	protected String getFileName() {
		return "testPurgeTitle.xml";
	}

	@Override
	protected void mockInValidCalls(PlaceholderMessage mesage) throws Exception {
		when(mayamClient.purgeTitle((PurgeTitle) anyObject())).thenReturn(
				MayamClientErrorCode.TITLE_UPDATE_FAILED);

	}

	@Override
	protected void verifyInValidCalls(PlaceholderMessage message)
			throws Exception {
		verify(mayamClient).purgeTitle((PurgeTitle) anyObject());

	}

}
