package com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators;

import java.math.BigInteger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import au.com.foxtel.cf.mam.pms.TxPackageType;

public class MSTxPackage {

	/**
	 * Creates a valid object of type TxPackageType
	 * 
	 * @param txPackage
	 * @param titleId
	 * @return
	 * @throws DatatypeConfigurationException
	 */
	public TxPackageType validTxPackage(TxPackageType txPackage, String titleId)
			throws DatatypeConfigurationException {

		txPackage.setItemID("abc123");
		txPackage.setClassification("PG");
		txPackage.setConsumerAdvise("Generally suitable for all");
		txPackage.setNumberOfSegments(new BigInteger("4"));
		HelperMethods method = new HelperMethods();
		XMLGregorianCalendar xmlCal = method.giveValidDate();
		txPackage.setTargetDate(xmlCal);
		txPackage.setNotes("none");
		txPackage.setAutoID("abc123");

		return txPackage;
	}
}
