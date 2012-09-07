package com.mediasmiths.foxtel.placeholder.messagecreation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidationResult;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidator;
import com.mediasmiths.mayam.MayamClient;

public abstract class PlaceHolderMessageTest {

	protected abstract PlaceholderMessage generatePlaceholderMessage () throws Exception;
	protected abstract String getFileName();
	
    protected MayamClient mayamClient = mock(MayamClient.class);
	
	private PlaceHolderMessageValidator toTest;
	
	protected String getFilePath(){
		return "/tmp/Foxtel/TestData/" + getFileName();
	}
	
	public PlaceHolderMessageTest() throws JAXBException, SAXException{
		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		toTest = new PlaceHolderMessageValidator(unmarhsaller, mayamClient);

	}
	
	@Test
	public final void testWritePlaceHolderMessage () throws Exception {
		
		PlaceholderMessage message = this.generatePlaceholderMessage();
		FileWriter writer = new FileWriter();
		writer.writeObjectToFile(message, getFilePath());
		//test that the generated placeholder message is valid
		assertEquals(PlaceHolderMessageValidationResult.IS_VALID,toTest.validateFile(getFilePath()));
		
	}
	
}
