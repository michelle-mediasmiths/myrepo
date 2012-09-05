package com.mediasmiths.foxtel.placeholder.messagecreation;

import com.mediasmiths.foxtel.placeholder.HelperMethods;
import com.mediasmiths.foxtel.placeholder.MSItem;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddItem;
import au.com.foxtel.cf.mam.pms.ItemType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

public class TestAddItem extends PlaceHolderMessageTest{

	protected PlaceholderMessage generatePlaceholderMessage()
			throws Exception {

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");

		HelperMethods method = new HelperMethods();
		String titleId = method.validTitleId();

		MSItem msItem = new MSItem();
		ItemType item = new ItemType();
		item = msItem.validItem(item, titleId);

		AddItem addItem = new AddItem();
		addItem.setTitleID(titleId);
		addItem.setItem(item);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddItem().add(addItem);
		message.setActions(actions);

		return message;
	}

	@Override
	protected String getFileName() {
		return "testAddItem.xml";
	}
}
