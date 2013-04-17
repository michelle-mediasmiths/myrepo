package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.messagecreation.elementgenerators;

import org.apache.commons.lang.RandomStringUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class HelperMethods {

	/**
	 * Generates a random date after todays date
	 * 
	 * @return xmlDate
	 * @throws javax.xml.datatype.DatatypeConfigurationException
	 */
	public XMLGregorianCalendar giveValidDate()
			throws DatatypeConfigurationException {
		return giveValidDate(Relative.AFTER,new GregorianCalendar());
	}

	public XMLGregorianCalendar giveValidDate(Relative relativeTo, Calendar calDate) throws DatatypeConfigurationException{

		int numberOfDaysToAdd = (int) (Math.random() * 730 + 1);

		if(relativeTo.equals(Relative.BEFORE)){
			numberOfDaysToAdd *= -1;
		}

		calDate.add(Calendar.DAY_OF_YEAR, numberOfDaysToAdd);

		GregorianCalendar gregDate = new GregorianCalendar();
		gregDate.setTimeInMillis(calDate.getTimeInMillis());

		XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(gregDate);
		return xmlDate;

	}

	enum Relative{
		BEFORE,AFTER;
	}

	/**
	 * Merges an array of xml files
	 *
	 * @param files
	 * @throws java.io.IOException
	 */
	public static void merge(String[] files) throws IOException {

		Writer output = null;
		output = new BufferedWriter(new FileWriter("merged.xml"));

		output.write("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
		output.write("\n<PlaceholderMessage xmlns=\"http://foxtel.com.au/cf/mam/pms/000/003\" messageID=\"def456\" senderID=\"abc123\">");
		output.write("\n<Actions>");

		for (int i = 0; i < files.length; i++) {
			FileInputStream in = new FileInputStream(files[i]);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				output.write("\n");
				output.write(strLine);
			}
			br.close();
		}
		output.write("</Actions>");
		output.write("\n</PlaceholderMessage>");
		output.close();

		System.out.println("Merge complete");
	}

	public String generateTitleID() {
		return RandomStringUtils.randomAlphanumeric(20);
	}
}
