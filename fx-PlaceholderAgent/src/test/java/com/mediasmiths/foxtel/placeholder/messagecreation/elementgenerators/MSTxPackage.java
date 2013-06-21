package com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators;

import au.com.foxtel.cf.mam.pms.ClassificationEnumType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;
import org.apache.commons.lang.RandomStringUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MSTxPackage {

	protected final List<PresentationFormatType> presentationformats = Collections.unmodifiableList(Arrays.asList(PresentationFormatType.values()));
	protected final List<ClassificationEnumType> classifications = Collections.unmodifiableList(Arrays.asList(ClassificationEnumType.values()));

	private final Random random = new Random();
	
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

		txPackage.setMaterialID(RandomStringUtils.randomAlphanumeric(6));
		txPackage.setPresentationFormat(presentationformats.get(random.nextInt(presentationformats.size())));
		txPackage.setClassification(classifications.get(random.nextInt(classifications.size())));
		txPackage.setConsumerAdvice(buildConsumerAdvice());
		txPackage.setNumberOfSegments(new BigInteger(""+(random.nextInt(9)+1)));
		HelperMethods method = new HelperMethods();
		XMLGregorianCalendar xmlCal = method.giveValidDate();
		txPackage.setTargetDate(xmlCal);
		txPackage.setNotes("notes");
		txPackage.setPresentationID(RandomStringUtils.randomAlphanumeric(6));

		return txPackage;
	}


	private String buildConsumerAdvice() {
		
		String[] allAdvice = new String[]{"Violence", "Language", "Sex", "Nudity"};
		
		int numberofadvisories = random.nextInt(allAdvice.length);
		int startat = random.nextInt(allAdvice.length);
		
		StringBuilder advice = new StringBuilder();
		for(int i=0,j=startat;i<numberofadvisories;i++){
			advice.append(allAdvice[j]);
			advice.append(' ');
			j++;
			if(j==allAdvice.length)j=0;
		}
			
		return advice.toString();
	}
}
