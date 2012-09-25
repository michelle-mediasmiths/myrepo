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
import com.mediasmiths.mayam.MayamClientErrorCode;

public class DeleteMaterialTest extends PlaceHolderMessageShortTest{
	
	public DeleteMaterialTest() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testDeleteMaterialProcessing() throws DatatypeConfigurationException, MessageProcessingFailedException {
		
		System.out.println("Delete material processing test");
		PlaceholderMessage message = buildDeleteMaterial(false, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		DeleteMaterial dm = (DeleteMaterial) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		when (mayamClient.deleteMaterial(dm)).thenReturn(MayamClientErrorCode.SUCCESS);
		
		processor.processMessage(envelope);
		
		verify(mayamClient).deleteMaterial(dm);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialXSDInvalid() throws Exception {
		
		System.out.println("FXT 4.1.7.2 - Non XSD compliance");
		File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		IOUtils.write("InvalidDeleteMaterial", new FileOutputStream(temp));
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialNotProtected() throws IOException, Exception {
		
		System.out.println("FXT 4.1.7.3/4/5 - XSD Compliance/ Valid DeleteMaterialMessage/ Matching ID exists");
		PlaceholderMessage message = buildDeleteMaterial(false, EXISTING_TITLE);
		File temp = createTempXMLFile(message, "validDeleteMaterialNotProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(false);
		when(mayamClient.materialExists(EXISTING_MATERIAL_ID)).thenReturn(true);
		
		assertEquals(MessageValidationResult.IS_VALID, validator.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialDoesntExist() throws Exception {
		
		System.out.println("FXT 4.1.7.6 - No matching ID");
		
		PlaceholderMessage message = buildDeleteMaterial(false, NOT_EXISTING_MATERIAL);
		File temp = createTempXMLFile (message, "deleteMaterialDoesntExist");
		
		when(mayamClient.materialExists(NOT_EXISTING_MATERIAL)).thenReturn(false);
		
		assertEquals(MessageValidationResult.MATERIAL_DOES_NOT_EXIST, validator.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	@Category (ValidationTests.class)
	public void testDeleteMaterialProtected() throws IOException, Exception {
		
		System.out.println("FXT 4.1.7.7 - Material is protected");
		PlaceholderMessage message = buildDeleteMaterial(false, EXISTING_TITLE);
		File temp = createTempXMLFile(message, "validDeleteMaterialProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(true);
		
		assertEquals(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED,
				validator.validateFile(temp.getAbsolutePath()));
	}

	private PlaceholderMessage buildDeleteMaterial (boolean materialProtected, String titleID) {
		
		Material material = new Material();
		material.setMaterialID(EXISTING_MATERIAL_ID);
		
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
