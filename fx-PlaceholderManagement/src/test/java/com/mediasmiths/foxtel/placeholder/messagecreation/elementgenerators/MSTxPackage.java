package com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators;

import java.math.BigInteger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import au.com.foxtel.cf.mam.pms.ClassificationEnumType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

public class MSTxPackage {

	/**
	 * Creates a valid object of type PackageType
	 * 
	 * @param txPackage
	 * @param titleId
	 * @return
	 * @throws DatatypeConfigurationException
	 */
	public PackageType validTxPackage(PackageType txPackage, String titleId)
			throws DatatypeConfigurationException {

		txPackage.setMaterialID("abc123");
		txPackage.setPresentationFormat(PresentationFormatType.HD);
		txPackage.setClassification(ClassificationEnumType.PG);
		txPackage.setConsumerAdvice("Generally suitable for all");
		txPackage.setNumberOfSegments(new BigInteger("4"));
		HelperMethods method = new HelperMethods();
		XMLGregorianCalendar xmlCal = method.giveValidDate();
		txPackage.setTargetDate(xmlCal);
		txPackage.setNotes("none");
		txPackage.setPresentationID("abc123");

		return txPackage;
	}
}
