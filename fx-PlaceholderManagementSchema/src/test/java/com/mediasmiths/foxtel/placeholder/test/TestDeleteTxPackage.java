package com.mediasmiths.foxtel.placeholder.test;

import com.mediasmiths.foxtel.placeholder.HelperMethods;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteTxPackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.TxPackage;

public class TestDeleteTxPackage {
	
	private PlaceholderMessage generatePlaceholderMessage () {
		
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");
		
		DeleteTxPackage deleteTx = generateDeleteTxPackage();

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddItem().add(deleteTx);
		message.setActions(actions);

		return message;
	}
	
	private DeleteTxPackage generateDeleteTxPackage () {
		
		DeleteTxPackage deleteTx = new DeleteTxPackage();
		HelperMethods method = new HelperMethods();
		String titleId = method.validTitleId();
		TxPackage txPackage = new TxPackage();
		txPackage.setAutoID("abc123");
		deleteTx.setTxPackage(txPackage);
		deleteTx.setTitleID(titleId);
		
		return deleteTx;
	}
	
	public static void main (String [] args) {
		
		TestDeleteTxPackage tdp = new TestDeleteTxPackage();
		PlaceholderMessage message = tdp.generatePlaceholderMessage();
		FileWriter writer = new FileWriter();
		
		try {
			writer.writeObjectToFile(message, "/Users/alisonboal/Documents/Foxtel/XMLTestFiles/testDeleteTxPackage.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
