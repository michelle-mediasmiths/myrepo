package com.mediasmiths.foxtel.placeholder.test;

import org.junit.Test;

import com.mediasmiths.foxtel.placeholder.HelperMethods;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

public class TestPurgeTitle {
	
	private PlaceholderMessage generatePlaceholderMessage () {
		
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");
		
		PurgeTitle purgeTitle = generatePurgeTitle();
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddItem().add(purgeTitle);
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
	
	@Test
	public void testPurgeTitle () throws Exception {
		
		TestPurgeTitle tpt = new TestPurgeTitle();
		PlaceholderMessage message = tpt.generatePlaceholderMessage();
		FileWriter writer = new FileWriter();
		writer.writeObjectToFile(message, "/Users/alisonboal/Documents/Foxtel/XMLTestFiles/testPurgeTitle.xml");
		
	}

}
