package com.mediasmiths.foxtel.placeholder.messagecreation;

import org.junit.Test;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

public abstract class PlaceHolderMessageTest {

	protected abstract PlaceholderMessage generatePlaceholderMessage () throws Exception;
	protected abstract String getFileName();
	protected String getFilePath(){
		return "/tmp/Foxtel/TestData/" + getFileName();
	}
	
	@Test
	public final void testWritePlaceHolderMessage () throws Exception {
		
		PlaceholderMessage message = this.generatePlaceholderMessage();
		FileWriter writer = new FileWriter();
		writer.writeObjectToFile(message, getFilePath());
		
	}
	
}
