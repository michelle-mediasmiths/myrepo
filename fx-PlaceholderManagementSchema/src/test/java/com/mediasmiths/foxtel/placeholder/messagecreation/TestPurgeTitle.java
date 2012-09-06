package com.mediasmiths.foxtel.placeholder.messagecreation;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;

public class TestPurgeTitle extends PlaceHolderMessageTest {
	
	protected PlaceholderMessage generatePlaceholderMessage () {
		
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");
		
		PurgeTitle purgeTitle = generatePurgeTitle();
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(purgeTitle);
		message.setActions(actions);
		
		return message;
	}
	
	private PurgeTitle generatePurgeTitle () {
		
		PurgeTitle purgeTitle = new PurgeTitle();
		HelperMethods method = new HelperMethods();
		String titleId = method.validTitleId();
		purgeTitle.setTitleID(titleId);
		
		return purgeTitle;
	}

	@Override
	protected String getFileName() {
		return "testPurgeTitle.xml";
	}
	
}
