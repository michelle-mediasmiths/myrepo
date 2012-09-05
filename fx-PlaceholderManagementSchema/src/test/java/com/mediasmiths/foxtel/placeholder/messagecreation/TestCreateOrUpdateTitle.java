package com.mediasmiths.foxtel.placeholder.messagecreation;

import javax.xml.datatype.DatatypeConfigurationException;

import com.mediasmiths.foxtel.placeholder.HelperMethods;
import com.mediasmiths.foxtel.placeholder.MSRights;
import com.mediasmiths.foxtel.placeholder.MSTitleDescription;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

public class TestCreateOrUpdateTitle extends PlaceHolderMessageTest{

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
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddItem().add(createTitle);

		message.setActions(actions);

		return message;
	}

	@Override
	protected String getFileName() {
		return "testCreateOrUpdateTitle.xml";
	}

}
