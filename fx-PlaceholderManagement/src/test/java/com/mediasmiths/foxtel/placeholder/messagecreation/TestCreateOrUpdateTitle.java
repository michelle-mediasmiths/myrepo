package com.mediasmiths.foxtel.placeholder.messagecreation;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.MSRights;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.MSTitleDescription;

public class TestCreateOrUpdateTitle extends PlaceHolderMessageTest{

	public TestCreateOrUpdateTitle() throws JAXBException, SAXException {
		super();
	}

	protected PlaceholderMessage generatePlaceholderMessage()
			throws DatatypeConfigurationException {

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");

		HelperMethods method = new HelperMethods();
		String titleId = method.validTitleId();

		MSTitleDescription msTitleDescription = new MSTitleDescription();
		TitleDescriptionType titleDescription = new TitleDescriptionType();
		titleDescription = msTitleDescription.validTitleDescription(
				titleDescription, titleId);

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
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(createTitle);

		message.setActions(actions);

		return message;
	}

	@Override
	protected String getFileName() {
		return "testCreateOrUpdateTitle.xml";
	}

}
