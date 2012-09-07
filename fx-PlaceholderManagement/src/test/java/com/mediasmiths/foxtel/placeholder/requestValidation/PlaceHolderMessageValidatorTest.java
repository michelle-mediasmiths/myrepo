package com.mediasmiths.foxtel.placeholder.requestValidation;

import static org.mockito.Mockito.mock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidator;
import com.mediasmiths.mayam.MayamClient;

public abstract class PlaceHolderMessageValidatorTest {

	protected PlaceHolderMessageValidator toTest;
	protected MayamClient mayamClient = mock(MayamClient.class);

	public PlaceHolderMessageValidatorTest() throws JAXBException, SAXException {

		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		toTest = new PlaceHolderMessageValidator(unmarhsaller, mayamClient);

	}

}
