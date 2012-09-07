package com.mediasmiths.foxtel.placeholder.requestValidation;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

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
