package com.mediasmiths.foxtel.messagetests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class DeleteMaterialTest_FXT_4_1_7 extends PlaceHolderMessageShortTest{
	private static Logger logger = Logger.getLogger(DeleteMaterialTest_FXT_4_1_7.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);


	public DeleteMaterialTest_FXT_4_1_7() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testDeleteMaterialProcessing() throws DatatypeConfigurationException, MessageProcessingFailedException {
		
		logger.info("Delete material processing test");
		PlaceholderMessage message = buildDeleteMaterial(false, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		DeleteMaterial dm = (DeleteMaterial) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		when (mayamClient.deleteMaterial(dm)).thenReturn(MayamClientErrorCode.SUCCESS);
		
		processor.processMessage(envelope);
		
		verify(mayamClient).deleteMaterial(dm);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialXSDInvalid_FXT_4_1_7_2() throws Exception {
		
		logger.info("Starting FXT 4.1.7.2 - Non XSD compliance");
	//	File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		File temp = new File("/tmp/placeHolderTestData/NonXSDConformingFile__"+RandomStringUtils.randomAlphabetic(6)+ ".xml");

		IOUtils.write("InvalidDeleteMaterial", new FileOutputStream(temp));
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.FAILS_XSD_CHECK ==validateFile)
			resultLogger.info("FXT 4.1.7.2 - Non XSD compliance --Passed");
		else
			resultLogger.info("FXT 4.1.7.2 - Non XSD compliance --Failed");
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);
		Util.deleteFiles(temp.getAbsolutePath());
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialNotProtected_FXT_4_1_7_3_4_5() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.7.3/4/5 - XSD Compliance/ Valid DeleteMaterialMessage/ Matching ID exists");
		PlaceholderMessage message = buildDeleteMaterial(false, EXISTING_TITLE);
		File temp = createTempXMLFile(message, "validDeleteMaterialNotProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(false);
		when(mayamClient.materialExists(EXISTING_MATERIAL_ID)).thenReturn(true);
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.IS_VALID ==validateFile)
			resultLogger.info("FXT 4.1.7.3/4/5  - XSD Compliance/ Valid DeleteMaterialMessage/ Matching ID exists --Passed");
		else
			resultLogger.info("FXT 4.1.7.3/4/5  - XSD Compliance/ Valid DeleteMaterialMessage/ Matching ID exists --Failed");
		
		assertEquals(MessageValidationResult.IS_VALID, validateFile);		Util.deleteFiles(temp.getAbsolutePath());
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialDoesntExist_FXT_4_1_7_6() throws Exception {
		
		logger.info("Starting FXT 4.1.7.6 - No matching ID");
		
		PlaceholderMessage message = buildDeleteMaterial(false, NOT_EXISTING_MATERIAL);
		File temp = createTempXMLFile (message, "deleteMaterialDoesntExist");
		
		when(mayamClient.materialExists(NOT_EXISTING_MATERIAL)).thenReturn(false);
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.MATERIAL_DOES_NOT_EXIST ==validateFile)
			resultLogger.info("FXT 4.1.7.6 - No matching ID --Passed");
		else
			resultLogger.info("FXT 4.1.7.6 - No matching ID --Failed");
		
		assertEquals(MessageValidationResult.MATERIAL_DOES_NOT_EXIST, validateFile);		
		Util.deleteFiles(temp.getAbsolutePath());
	}
	
	@Test
	@Category (ValidationTests.class)
	public void testDeleteMaterialProtected_FXT_4_1_7_7() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.7.7 - Material is protected");
		PlaceholderMessage message = buildDeleteMaterial(false, EXISTING_TITLE);
		File temp = createTempXMLFile(message, "validDeleteMaterialProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(true);
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED ==validateFile)
			resultLogger.info("FXT 4.1.7.7 - Material is protected --Passed");
		else
			resultLogger.info("FXT 4.1.7.7 - Material is protected --Failed");
		
		assertEquals(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED, validateFile);
		Util.deleteFiles(temp.getAbsolutePath());
	}

	private PlaceholderMessage buildDeleteMaterial (boolean materialProtected, String titleID) {
		
		Material material = new Material();
		material.setMaterialD(EXISTING_MATERIAL_ID);
		
		DeleteMaterial dm = new DeleteMaterial();
		dm.setTitleID(titleID);
		dm.setMaterial(material);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(dm);
		
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID(MESSAGE_ID);
		message.setSenderID(SENDER_ID);
		message.setActions(actions);
		
		return message;
	}

}
