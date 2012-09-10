package com.mediasmiths.foxtel.placeholder.requestValidation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidationResult;
import com.mediasmiths.mayam.MayamClientException;

public class NonXSDConformingTest extends PlaceHolderMessageValidatorTest{
	
	private static final String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed in quam pretium ipsum laoreet sollicitudin vel condimentum ante. Vestibulum eget diam dolor, at porta elit. Morbi velit dolor, bibendum sed accumsan ac, ornare ac elit. Curabitur tellus sapien, laoreet sed aliquet quis, condimentum ut erat. Nullam accumsan, nisi vitae pulvinar pulvinar, magna lorem tristique massa, et commodo orci diam vitae ipsum. Aliquam sit amet magna urna. Cras et nulla sed orci luctus suscipit et at metus. Praesent rhoncus imperdiet dapibus. Nullam lacus purus, pretium pellentesque lacinia quis, dapibus et diam. Nam sapien justo, fermentum a consectetur sit amet, dignissim a nisi";
	
	public NonXSDConformingTest() throws JAXBException, SAXException {
		super();		
	}

	@Test
	public void testNonXSDConformingFileFails() throws IOException, SAXException, ParserConfigurationException, MayamClientException{
		
		
		File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		IOUtils.write(loremIpsum, new FileOutputStream(temp));		
		PlaceHolderMessageValidationResult validateFile = toTest.validateFile(temp.getAbsolutePath());
		
		assertEquals(PlaceHolderMessageValidationResult.FAILS_XSD_CHECK, validateFile);
		
	}

}
