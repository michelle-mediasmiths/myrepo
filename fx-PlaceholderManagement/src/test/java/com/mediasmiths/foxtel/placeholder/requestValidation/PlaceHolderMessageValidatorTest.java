package com.mediasmiths.foxtel.placeholder.requestValidation;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidator;
import com.mediasmiths.foxtel.placeholder.messagecreation.FileWriter;
import com.mediasmiths.mayam.MayamClient;

public abstract class PlaceHolderMessageValidatorTest {

	protected PlaceHolderMessageValidator toTest;
	protected MayamClient mayamClient = mock(MayamClient.class);
	
	protected final static String MESSAGE_ID = "123456asdfg";
	protected final static String SENDER_ID = "123456asdfg";

	protected final static GregorianCalendar JAN1st = new GregorianCalendar(2000, 1, 1, 0, 0, 1);
	protected final static GregorianCalendar JAN10th = new GregorianCalendar(2000, 1, 10, 0, 0, 1);
	
	
	public PlaceHolderMessageValidatorTest() throws JAXBException, SAXException {

		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		toTest = new PlaceHolderMessageValidator(unmarhsaller, mayamClient);

	}


	protected File createTempXMLFile(PlaceholderMessage pm, String description) throws IOException,
			Exception {
		//marshall and write to file
		FileWriter writer = new FileWriter();
		File temp = File.createTempFile(description, ".xml");		
		writer.writeObjectToFile(pm, temp.getAbsolutePath());
		return temp;
	}
	
}
