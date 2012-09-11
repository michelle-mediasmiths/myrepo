package com.mediasmiths.foxtel.placeholder.validation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.messagecreation.FileWriter;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;

public class UnmarshallFailureTest {

	protected MessageValidator toTest;
	protected MayamClient mayamClient = mock(MayamClient.class);
	protected Unmarshaller unmarshaller = mock(Unmarshaller.class);
	
	@Before
	public void beforeTest() throws SAXException{
		toTest = new MessageValidator(unmarshaller, mayamClient); 
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testUnmarshallFailure() throws Exception{
	
		PlaceholderMessage pm = createMessage();		
		File temp = writeFile("UnmarshallFailure",pm);
				
		when(unmarshaller.unmarshal(temp)).thenThrow(new JAXBException("test jaxbexception"));
		
		assertEquals(MessageValidationResult.FAILED_TO_UNMARSHALL, toTest.validateFile(temp.getAbsolutePath()));
	}

	@Test
	@Category(ValidationTests.class)
	public void testUnexpectedTypeAfterUnmarshalling() throws Exception{
		
		PlaceholderMessage pm = createMessage();		
		File temp = writeFile("UnexpectedTypeAfterMarshalling",pm);
				
		when(unmarshaller.unmarshal(temp)).thenReturn(new String("not a placeholder message"));
		
		assertEquals(MessageValidationResult.UNEXPECTED_TYPE, toTest.validateFile(temp.getAbsolutePath()));
	}
	
	
	private PlaceholderMessage createMessage()
			throws DatatypeConfigurationException {
		PlaceholderMessage pm = AddOrUpdateMaterialTest.buildAddMaterialRequest("TITLE", new GregorianCalendar(2000, 1, 1, 0, 0, 1), new GregorianCalendar(2000, 1, 10, 0, 0, 1));
		return pm;
	}

	private File writeFile(String description, PlaceholderMessage pm) throws IOException, Exception {
		FileWriter writer = new FileWriter();
		File temp = File.createTempFile(description, ".xml");
		writer.writeObjectToFile(pm, temp.getAbsolutePath());
		return temp;
	}
	
	
}
