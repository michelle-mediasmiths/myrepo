package com.mediasmiths.foxtel.placeholder.test;

import org.junit.Test;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteItem;
import au.com.foxtel.cf.mam.pms.Item;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

public class TestDeleteItem {

	private PlaceholderMessage generatePlaceholderMessage() {
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

	@Test
	public void testDeleteItem() throws Exception {
		TestDeleteItem tdi = new TestDeleteItem();
		PlaceholderMessage message = tdi.generatePlaceholderMessage();
		FileWriter writer = new FileWriter();

		writer.writeObjectToFile(message,
				"/Users/alisonboal/Documents/Foxtel/XMLTestFiles/testDeleteItem.xml");

	}
}
