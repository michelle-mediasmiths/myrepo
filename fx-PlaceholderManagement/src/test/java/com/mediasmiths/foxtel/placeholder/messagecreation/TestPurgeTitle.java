package com.mediasmiths.foxtel.placeholder.messagecreation;

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

public class TestPurgeTitle extends PlaceHolderMessageTest {

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
	protected void mockCalls(PlaceholderMessage mesage) throws Exception {
		when(mayamClient.purgeTitle((PurgeTitle) anyObject())).thenReturn(MayamClientErrorCode.SUCCESS);
	}
	
	@Override
	protected void verifyCalls(PlaceholderMessage message){
		verify(mayamClient).purgeTitle((PurgeTitle) anyObject());
	}

	@Override
	protected String getFileName() {
		return "testPurgeTitle.xml";
	}

}
