package com.mediasmiths.foxtel.placeholder.test;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Test;

import com.mediasmiths.foxtel.placeholder.HelperMethods;
import com.mediasmiths.foxtel.placeholder.MSTxPackage;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateTxPackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.TxPackageType;

public class TestAddOrUpdateTxPackage {

	private PlaceholderMessage generatePlaceholderMessage()
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

	@Test
	public void testAddOrUpdateTxPackage()
 throws Exception {
		TestAddOrUpdateTxPackage tap = new TestAddOrUpdateTxPackage();
		PlaceholderMessage message = tap.generatePlaceholderMessage();
		FileWriter writer = new FileWriter();
		writer.writeObjectToFile(message,
					"/Users/alisonboal/Documents/Foxtel/XMLTestFiles/testAddOrUpdateTxPackage.xml");
		

	}

}
