package com.mediasmiths.foxtel.messagetests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
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
import com.mediasmiths.foxtel.placeholder.junit.categories.ValidationTests;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class DeletePackageTest extends PlaceHolderMessageShortTest {
	
	public DeletePackageTest() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testDeletePackageProcessing() throws MessageProcessingFailedException {
		
		System.out.println ("Delete package processing test");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, EXISTING_PACKAGE_ID);
		MessageEnvelope <PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		DeletePackage dp = (DeletePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		when(mayamClient.deletePackage(dp)).thenReturn(MayamClientErrorCode.SUCCESS);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageXSDInvalid() throws Exception {
		
		System.out.println("FXT 4.1.11.2 - Non XSD compliance");
		File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		IOUtils.write("InvalidDeletePackage", new FileOutputStream(temp));
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageNotProtected () throws IOException, Exception {
		
		System.out.println("FXT 4.1.11.3/4/5 - XSD Compliance/ Valid DeletePackage message/ Matching ID exists");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, EXISTING_PACKAGE_ID);
		File temp = createTempXMLFile(message, "validDeletePackageNotProtected");
		
		when(mayamClient.isMaterialForPackageProtected(EXISTING_PACKAGE_ID)).thenReturn(new Boolean(false));
		when(mayamClient.packageExists(EXISTING_PACKAGE_ID)).thenReturn(true);
		
		assertEquals(MessageValidationResult.IS_VALID, validator.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	@Category (ValidationTests.class) 
	public void testDeletePackageDoesntExist() throws Exception {
		
		System.out.println("FXT 4.1.11.6 - No matching ID");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, NOT_EXISTING_PACKAGE);
		File temp = createTempXMLFile (message, "deletePackageDoesntExist");
		
		when(mayamClient.packageExists(NOT_EXISTING_PACKAGE)).thenReturn(false);
		
		assertEquals(MessageValidationResult.PACKAGE_DOES_NOT_EXIST, validator.validateFile(temp.getAbsolutePath()));
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageProtected () throws IOException, Exception {
		
		System.out.println("FXT 4.1.11.7 - Package is protected");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, EXISTING_PACKAGE_ID);
		File temp = createTempXMLFile(message, "validDeletePackageProtected");
		
		when(mayamClient.isMaterialForPackageProtected(EXISTING_PACKAGE_ID)).thenReturn(true);
		
		assertEquals(MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED, validator.validateFile(temp.getAbsolutePath()));
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
		message.setMessageID(MESSAGE_ID);
		message.setSenderID(SENDER_ID);
		message.setActions(actions);
		return message;
	}
}