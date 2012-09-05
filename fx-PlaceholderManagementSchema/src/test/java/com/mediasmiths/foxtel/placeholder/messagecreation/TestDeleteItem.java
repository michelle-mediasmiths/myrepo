package com.mediasmiths.foxtel.placeholder.messagecreation;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteItem;
import au.com.foxtel.cf.mam.pms.Item;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

public class TestDeleteItem extends PlaceHolderMessageTest{

	protected PlaceholderMessage generatePlaceholderMessage() throws Exception {
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");

		DeleteItem deleteItem = new DeleteItem();
		deleteItem.setTitleID("Gavin and Stacey");

		Item item = new Item();
		item.setItemID("fgh456");
		deleteItem.setItem(item);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddItem().add(deleteItem);
		message.setActions(actions);

		return message;
	}

	@Override
	protected String getFileName() {
		return "testDeleteItem.xml";
	}
}
