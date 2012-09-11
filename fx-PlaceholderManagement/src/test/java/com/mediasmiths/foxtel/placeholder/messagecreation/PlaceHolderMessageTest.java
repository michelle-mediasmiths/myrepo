package com.mediasmiths.foxtel.placeholder.messagecreation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.PlaceHolderManagerConfiguration;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;

public abstract class PlaceHolderMessageTest {

	protected abstract PlaceholderMessage generatePlaceholderMessage () throws Exception;
	protected abstract String getFileName();
	
    protected MayamClient mayamClient = mock(MayamClient.class);
	
	private MessageValidator toTest;
	
	protected String getFilePath() throws IOException{
		return PlaceHolderManagerConfiguration.getInstance().getMessagePath() + IOUtils.DIR_SEPARATOR + getFileName();
	}
	
	public PlaceHolderMessageTest() throws JAXBException, SAXException{
		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		toTest = new MessageValidator(unmarhsaller, mayamClient);

	}
	
	protected void mockCalls() {
		
	}
	
	@Test
	public final void testWritePlaceHolderMessage () throws Exception {
		
		PlaceholderMessage message = this.generatePlaceholderMessage();
		FileWriter writer = new FileWriter();
		writer.writeObjectToFile(message, getFilePath());
		mockCalls();
		//test that the generated placeholder message is valid
		assertEquals(MessageValidationResult.IS_VALID,toTest.validateFile(getFilePath()));
		
	}
	
}
