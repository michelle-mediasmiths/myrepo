package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.validation.PlaceholderMessageValidator;
import com.mediasmiths.foxtel.placeholder.validation.ReceiptWriterThatAlwaysReturnsUniqueFiles;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.FileWriter;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.validation.MayamValidator;

public class UnmarshallFailureTest {

	protected PlaceholderMessageValidator toTest;
	protected MayamClient mayamClient = mock(MayamClient.class);
	protected MayamValidator mayamValidator = mock(MayamValidator.class);
	protected Unmarshaller unmarshaller = mock(Unmarshaller.class);
	protected ChannelProperties channelValidator = mock(ChannelProperties.class);	
	
	@Before
	public void beforeTest() throws SAXException{
		toTest = new PlaceholderMessageValidator(unmarshaller, mayamClient,mayamValidator,new ReceiptWriterThatAlwaysReturnsUniqueFiles("/tmp/placeHolderTestData"), new SchemaValidator("PlaceholderManagement.xsd"), channelValidator); 
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testUnmarshallFailure() throws Exception{
	
		PlaceholderMessage pm = createMessage();		
		File temp = writeFile("UnmarshallFailure",pm);
				
		when(unmarshaller.unmarshal(temp)).thenThrow(new JAXBException("test jaxbexception"));
			
		assertEquals(MessageValidationResult.FAILED_TO_UNMARSHALL, toTest.validatePickupPackage(temp.getAbsolutePath()));
	}

	@Test
	@Category(ValidationTests.class)
	public void testUnexpectedTypeAfterUnmarshalling() throws Exception{
		
		PlaceholderMessage pm = createMessage();		
		File temp = writeFile("UnexpectedTypeAfterMarshalling",pm);
		
		
				
		when(unmarshaller.unmarshal(temp)).thenReturn(new String("not a placeholder message"));
		
		
		assertEquals(MessageValidationResult.UNEXPECTED_TYPE, toTest.validatePickupPackage(temp.getAbsolutePath()));
	}
	
	
	private PlaceholderMessage createMessage()
			throws DatatypeConfigurationException {
		PlaceholderMessage pm = AddOrUpdateMaterialTest.buildAddMaterialRequest("TITLE", new GregorianCalendar(2000, 1, 1, 0, 0, 1), new GregorianCalendar(2000, 1, 10, 0, 0, 1));
		return pm;
	}

	private File writeFile(String description, PlaceholderMessage pm) throws IOException, Exception {
		FileWriter writer = new FileWriter();
		//File temp = File.createTempFile(description, ".xml");
		File temp = new File("/tmp/placeHolderTestData/"+"description"+"__"+RandomStringUtils.randomAlphabetic(6)+ ".xml");

		writer.writeObjectToFile(pm, temp.getAbsolutePath());
		return temp;
	}
	
	
}
