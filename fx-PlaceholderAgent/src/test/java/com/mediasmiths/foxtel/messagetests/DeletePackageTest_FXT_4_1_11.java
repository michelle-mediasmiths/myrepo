package com.mediasmiths.foxtel.messagetests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.Package;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class DeletePackageTest_FXT_4_1_11 extends PlaceHolderMessageShortTest {
	
	private static Logger logger = Logger.getLogger(DeletePackageTest_FXT_4_1_11.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	
	public DeletePackageTest_FXT_4_1_11() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testDeletePackageProcessing() throws MessageProcessingFailedException {
		
		logger.info ("Delete package processing test");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, EXISTING_PACKAGE_ID);
		MessageEnvelope <PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		DeletePackage dp = (DeletePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		when(mayamClient.deletePackage(dp)).thenReturn(MayamClientErrorCode.SUCCESS);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageXSDInvalid_FXT_4_1_11_2() throws Exception {
		
		logger.info("Starting FXT 4.1.11.2 - Non XSD compliance");
		//File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		File temp = new File("/tmp/placeHolderTestData/NonXSDConformingFile__"+RandomStringUtils.randomAlphabetic(6)+ ".xml");
		
		IOUtils.write("InvalidDeletePackage", new FileOutputStream(temp));
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.FAILS_XSD_CHECK ==validateFile)
			resultLogger.info("FXT 4.1.11.2 - Non XSD compliance --Passed");
		else
			resultLogger.info("FXT 4.1.11.2 - Non XSD compliance --Failed");
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);	
		Util.deleteFiles(temp.getAbsolutePath());
		
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageNotProtected_FXT_4_1_11_3_4_5 () throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.11.3/4/5 - XSD Compliance/ Valid DeletePackage message/ Matching ID exists");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, EXISTING_PACKAGE_ID);
		File temp = createTempXMLFile(message, "validDeletePackageNotProtected");
		
		when(mayamClient.isMaterialForPackageProtected(EXISTING_PACKAGE_ID,EXISTING_TITLE)).thenReturn(new Boolean(false));
		when(mayamClient.packageExistsForTitle(EXISTING_PACKAGE_ID, EXISTING_TITLE)).thenReturn(true);
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.IS_VALID ==validateFile)
			resultLogger.info("FXT  4.1.11.3/4/5 - XSD Compliance/ Valid DeletePackage message/ Matching ID exists--Passed");
		else
			resultLogger.info("FXT  4.1.11.3/4/5 - XSD Compliance/ Valid DeletePackage message/ Matching ID exists --Failed");
		
		assertEquals(MessageValidationResult.IS_VALID, validateFile);	
		Util.deleteFiles(temp.getAbsolutePath());
	}
	
	@Test
	@Category (ValidationTests.class) 
	public void testDeletePackageDoesntExist_FXT_4_1_11_6() throws Exception {
		
		logger.info("Starting FXT 4.1.11.6 - No matching ID");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, NOT_EXISTING_PACKAGE);
		File temp = createTempXMLFile (message, "deletePackageDoesntExist");
		
		when(mayamClient.packageExistsForTitle(NOT_EXISTING_PACKAGE,EXISTING_TITLE)).thenReturn(false);
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.PACKAGE_DOES_NOT_EXIST ==validateFile)
			resultLogger.info("FXT 4.1.11.6 - No matching ID --Passed");
		else
			resultLogger.info("FXT 4.1.11.6 - No matching ID --Failed");
		
		assertEquals(MessageValidationResult.PACKAGE_DOES_NOT_EXIST, validateFile);	
		Util.deleteFiles(temp.getAbsolutePath());
		
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageProtected_FXT_4_1_11_7 () throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.11.7 - Package is protected");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, PROTECTED_PACKAGE);
		File temp = createTempXMLFile(message, "validDeletePackageProtected");
		
		when(mayamClient.isMaterialForPackageProtected(PROTECTED_PACKAGE,EXISTING_TITLE)).thenReturn(true);
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED ==validateFile)
			resultLogger.info("FXT 4.1.11.7 - Package is protected --Passed");
		else
			resultLogger.info("FXT 4.1.11.7 - Package is protected --Failed");
		
		assertEquals(MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED, validateFile);	
		Util.deleteFiles(temp.getAbsolutePath());
	}

	public PlaceholderMessage buildDeletePackage (boolean b, String titleID, String packageID) {
		
		Package pack = new Package();
		pack.setPresentationID(packageID);
		
		DeletePackage dp = new DeletePackage();
		dp.setTitleID(titleID);
		dp.setPackage(pack);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(dp);
		
		PlaceholderMessage message  = new PlaceholderMessage();
		message.setMessageID(createMessageID());
		message.setSenderID(createSenderID());
		message.setActions(actions);
		return message;
	}
}
