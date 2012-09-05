package com.mediasmiths.foxtel.placeholder.messagecreation;

import javax.xml.datatype.DatatypeConfigurationException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateTxPackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.TxPackageType;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.MSTxPackage;

public class TestAddOrUpdateTxPackage extends PlaceHolderMessageTest{

	protected PlaceholderMessage generatePlaceholderMessage()
			throws DatatypeConfigurationException {

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");

		AddOrUpdateTxPackage addTxPackage = generateAddOrUpdateTxPackage();

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddItem().add(addTxPackage);
		message.setActions(actions);

		return message;
	}

	private AddOrUpdateTxPackage generateAddOrUpdateTxPackage()
			throws DatatypeConfigurationException {

		AddOrUpdateTxPackage addTxPackage = new AddOrUpdateTxPackage();

		HelperMethods method = new HelperMethods();
		String titleId = method.validTitleId();
		MSTxPackage msTxPackage = new MSTxPackage();
		TxPackageType txPackage = new TxPackageType();
		txPackage = msTxPackage.validTxPackage(txPackage, titleId);

		addTxPackage.setTitleID(titleId);
		addTxPackage.setTxPackage(txPackage);

		return addTxPackage;
	}

	@Override
	protected String getFileName() {
		return "testAddOrUpdateTxPackage.xml";
	}

}
