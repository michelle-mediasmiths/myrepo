package com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class HelperMethods {

	/**
	 * Generates a list of program titles that can be used in the titleID
	 * 
	 * @param programTitles
	 */
	public void generateList(ArrayList<String> programTitles) {

		String[] programTitlesArray = { "Absolutley Fabulous",
				"AfterYou've Gone", "Allo, Allo", "Basil Brush", "Black Books",
				"Crystal Maze", "Dad's Army", "Doctor Who", "Fawlty Towers",
				"Gavin and Stacey", "Horizon", "Hustle", "I'm Alan Partridge",
				"Jeeves and Wooster", "Keeping Up Appearances", "Life on Mars",
				"Max and Paddy's Road To Nowhere", "Mr Bean", "New Tricks",
				"Not Going Out", "Only Fools and Horses", "Outnumbered",
				"Planet Earth", "QI", "Question of Sport", "Rab C Nesbitt",
				"Silent Witness", "Some Mothers Do Av Em", "Spooks",
				"Top Gear", "Torchwood", "Ultimate Force", "Vicar of Dibley",
				"Waiting For God", "Whose Line Is It Anyway", "Yes Minister" };

		for (int i = 0; i < programTitlesArray.length; i++) {
			programTitles.add(programTitlesArray[i]);
		}
	}

	/**
	 * Creates a titleID using the generated list of program titles
	 * 
	 * @return titleID
	 */
	public String validTitleId() {

		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(36);
		ArrayList<String> programTitles = new ArrayList<String>();
		generateList(programTitles);
		String titleId = programTitles.get(randomInt) + "S04E03";

		return titleId;
	}

	/**
	 * Generates a random date after todays date
	 * 
	 * @return xmlDate
	 * @throws DatatypeConfigurationException
	 */
	public XMLGregorianCalendar giveValidDate()
			throws DatatypeConfigurationException {

		Calendar calDate = new GregorianCalendar();
		int numberOfDaysToAdd = (int) (Math.random() * 730 + 1);
		calDate.add(Calendar.DAY_OF_YEAR, numberOfDaysToAdd);

		GregorianCalendar gregDate = new GregorianCalendar();
		gregDate.setTimeInMillis(calDate.getTimeInMillis());

		XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(gregDate);
		return xmlDate;
	}

	/**
	 * Merges an array of xml files
	 * 
	 * @param files
	 * @throws IOException
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
		}
		output.write("</Actions>");
		output.write("\n</PlaceholderMessage>");
		output.close();

		System.out.println("Merge complete");
	}
}
