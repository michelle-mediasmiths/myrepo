package com.mediasmiths.foxtel.placeholder.validmessagepickup;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.lang.RandomStringUtils;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.MSRights;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.MSTitleDescription;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class TestCreateOrUpdateTitle extends ValidMessagePickTest {

	public TestCreateOrUpdateTitle() throws JAXBException, SAXException {
		super();
	}

	public PlaceholderMessage generatePlaceholderMessage()
			throws DatatypeConfigurationException {

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID(RandomStringUtils.randomAlphabetic(6));
		message.setSenderID(RandomStringUtils.randomAlphabetic(6));

		HelperMethods method = new HelperMethods();
		String titleId = method.generateTitleID();

		MSTitleDescription msTitleDescription = new MSTitleDescription();
		TitleDescriptionType titleDescription = new TitleDescriptionType();
		titleDescription = msTitleDescription.validTitleDescription(
				titleDescription);

		MSRights msRights = new MSRights();
		RightsType rights = new RightsType();
		rights = msRights.validRights(rights);

		CreateOrUpdateTitle createTitle = new CreateOrUpdateTitle();
		createTitle.setTitleID(titleId);
		createTitle.setTitleDescription(titleDescription);
		createTitle.setRights(rights);
		createTitle.setRestrictAccess(true);
		createTitle.setPurgeProtect(false);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				createTitle);

		message.setActions(actions);

		return message;
	}

	@Override
	protected void mockValidCalls(PlaceholderMessage message) throws Exception {
		mockCalls(message, MayamClientErrorCode.SUCCESS);
	}

	@Override
	protected void verifyValidCalls(PlaceholderMessage message) throws MayamClientException {
		verifyCalls(message);
	}

	private void mockCalls(PlaceholderMessage message,
			MayamClientErrorCode result) throws MayamClientException {
		CreateOrUpdateTitle createTitle = (CreateOrUpdateTitle) message
				.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);

		when(mayamClient.titleExists(createTitle.getTitleID())).thenReturn(
				new Boolean(false));
		when(mayamClient.createTitle((CreateOrUpdateTitle) anyObject()))
				.thenReturn(result);
	}

	private void verifyCalls(PlaceholderMessage message) throws MayamClientException {
		CreateOrUpdateTitle createTitle = (CreateOrUpdateTitle) message
				.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);

		verify(mayamClient).titleExists(createTitle.getTitleID());
		verify(mayamClient).createTitle((CreateOrUpdateTitle) anyObject());
	}

	@Override
	protected String getFileName() {
		return "testCreateOrUpdateTitle.xml";
	}

	@Override
	protected void mockInValidCalls(PlaceholderMessage message)
			throws Exception {
		mockCalls(message, MayamClientErrorCode.PACKAGE_UPDATE_FAILED);
	}

	@Override
	protected void verifyInValidCalls(PlaceholderMessage message)
			throws Exception {
		verifyCalls(message);

	}

}
