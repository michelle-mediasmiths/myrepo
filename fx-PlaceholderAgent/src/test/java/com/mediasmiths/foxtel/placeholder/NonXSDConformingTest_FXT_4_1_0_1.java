package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.messagetests.ResultLogger;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.util.Util;

public class NonXSDConformingTest_FXT_4_1_0_1 extends PlaceHolderMessageShortTest{
	private static Logger logger = Logger.getLogger(NonXSDConformingTest_FXT_4_1_0_1.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);
	private static final String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed in quam pretium ipsum laoreet sollicitudin vel condimentum ante. Vestibulum eget diam dolor, at porta elit. Morbi velit dolor, bibendum sed accumsan ac, ornare ac elit. Curabitur tellus sapien, laoreet sed aliquet quis, condimentum ut erat. Nullam accumsan, nisi vitae pulvinar pulvinar, magna lorem tristique massa, et commodo orci diam vitae ipsum. Aliquam sit amet magna urna. Cras et nulla sed orci luctus suscipit et at metus. Praesent rhoncus imperdiet dapibus. Nullam lacus purus, pretium pellentesque lacinia quis, dapibus et diam. Nam sapien justo, fermentum a consectetur sit amet, dignissim a nisi";
	
	public NonXSDConformingTest_FXT_4_1_0_1() throws JAXBException, SAXException, IOException {
		super();		
	}

	@Test
	@Category(ValidationTests.class)
	public void testNonXSDConformingFileFails() throws Exception{
		logger.info("Starting FXT 4.1.0.1  - Non XSD Compliance");
		
		File temp = File.createTempFile("NonXSDConformingFile", ".xml");

		IOUtils.write(loremIpsum, new FileOutputStream(temp));		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		
		if (MessageValidationResult.FAILS_XSD_CHECK ==validateFile)
			resultLogger.info("FXT 4.1.0.1  - Non XSD Compliance --Passed");
		else
			resultLogger.info("FXT 4.1.0.1  - Non XSD Compliance --Failed");
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);
		
		Util.deleteFiles(temp.getAbsolutePath());
	}

}
