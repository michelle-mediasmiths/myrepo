package com.mediasmiths.foxtel.placeholder.test;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddItem;
import au.com.foxtel.cf.mam.pms.ItemType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import javax.xml.datatype.DatatypeConfigurationException;

import com.mediasmiths.foxtel.placeholder.HelperMethods;
import com.mediasmiths.foxtel.placeholder.MSItem;

public class TestAddItem {

	private PlaceholderMessage generatePlaceholderMessage()
			throws DatatypeConfigurationException {

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

	public static void main(String[] args)
			throws DatatypeConfigurationException {

		TestAddItem tai = new TestAddItem();
		PlaceholderMessage message = tai.generatePlaceholderMessage();
		FileWriter writer = new FileWriter();

		try {
			writer.writeObjectToFile(message,
					"/Users/alisonboal/Documents/Foxtel/XMLTestFiles/testAddItem.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
