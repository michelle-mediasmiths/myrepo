package com.mediasmiths.foxtel.mpa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import static org.mockito.Mockito.mock;

import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.mpa.guice.MediaPickupModule;
import com.mediasmiths.foxtel.mpa.validation.MaterialExchangeValidator;
import com.mediasmiths.mayam.MayamClient;

public class NonXSDConformingTest{
	
	private static final String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed in quam pretium ipsum laoreet sollicitudin vel condimentum ante. Vestibulum eget diam dolor, at porta elit. Morbi velit dolor, bibendum sed accumsan ac, ornare ac elit. Curabitur tellus sapien, laoreet sed aliquet quis, condimentum ut erat. Nullam accumsan, nisi vitae pulvinar pulvinar, magna lorem tristique massa, et commodo orci diam vitae ipsum. Aliquam sit amet magna urna. Cras et nulla sed orci luctus suscipit et at metus. Praesent rhoncus imperdiet dapibus. Nullam lacus purus, pretium pellentesque lacinia quis, dapibus et diam. Nam sapien justo, fermentum a consectetur sit amet, dignissim a nisi";
	
	public NonXSDConformingTest() throws JAXBException, SAXException, IOException {
		super();		
	}

	@Test
	public void testNonXSDConformingFileFails() throws Exception{
		
		
		File temp = File.createTempFile("NonXSDConformingFile", ".xml");

		IOUtils.write(loremIpsum, new FileOutputStream(temp));		
		
		MediaPickupModule mpa = new MediaPickupModule();
		Unmarshaller um = mpa.provideUnmarshaller(mpa.provideJAXBContext());
		MayamClient mc = mock(MayamClient.class);
		ReceiptWriter rw = new ReceiptWriter(Util.prepareTempFolder("RECEIPT"));
		SchemaValidator sv = new SchemaValidator("MaterialExchange_V2.0.xsd");
		
		MaterialExchangeValidator validator = new MaterialExchangeValidator(um, mc, rw, sv);
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);
		
		Util.deleteFiles(temp.getAbsolutePath());
	}

}
