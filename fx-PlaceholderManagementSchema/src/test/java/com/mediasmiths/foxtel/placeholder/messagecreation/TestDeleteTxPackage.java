package com.mediasmiths.foxtel.placeholder.messagecreation;

import com.mediasmiths.foxtel.placeholder.HelperMethods;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteTxPackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.TxPackage;

public class TestDeleteTxPackage extends PlaceHolderMessageTest {
	
	protected PlaceholderMessage generatePlaceholderMessage () throws Exception {
		
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

	@Override
	protected String getFileName() {
		return "testDeleteTxPackage.xml";
	}

}
