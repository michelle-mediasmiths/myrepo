package com.mediasmiths.foxtel.placeholder.messagecreation;

import javax.xml.datatype.DatatypeConfigurationException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.MSTxPackage;

public class TestAddOrUpdatePackage extends PlaceHolderMessageTest{

	protected PlaceholderMessage generatePlaceholderMessage()
			throws DatatypeConfigurationException {

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");

		AddOrUpdatePackage addTxPackage = generateAddOrUpdatePackage();

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(addTxPackage);
		message.setActions(actions);

		return message;
	}

	private AddOrUpdatePackage generateAddOrUpdatePackage()
			throws DatatypeConfigurationException {

		AddOrUpdatePackage addTxPackage = new AddOrUpdatePackage();

		HelperMethods method = new HelperMethods();
		String titleId = method.validTitleId();
		MSTxPackage msTxPackage = new MSTxPackage();
		PackageType txPackage = new PackageType();
		txPackage = msTxPackage.validTxPackage(txPackage, titleId);

		addTxPackage.setTitleID(titleId);
		addTxPackage.setPackage(txPackage);

		return addTxPackage;
	}

	@Override
	protected String getFileName() {
		return "testAddOrUpdatePackage.xml";
	}

}
