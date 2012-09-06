package com.mediasmiths.foxtel.placeholder.messagecreation;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.Package;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;

public class TestDeletePackage extends PlaceHolderMessageTest {
	
	protected PlaceholderMessage generatePlaceholderMessage () throws Exception {
		
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");
		
		DeletePackage deleteTx = generateDeletePackage();

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(deleteTx);
		message.setActions(actions);

		return message;
	}
	
	private DeletePackage generateDeletePackage () {
		
		DeletePackage deleteTx = new DeletePackage();
		HelperMethods method = new HelperMethods();
		String titleId = method.validTitleId();
		Package txPackage = new Package();
		txPackage.setPresentationID("abc123");
		deleteTx.setPackage(txPackage);
		deleteTx.setTitleID(titleId);
		
		return deleteTx;
	}

	@Override
	protected String getFileName() {
		return "testDeletePackage.xml";
	}

}
