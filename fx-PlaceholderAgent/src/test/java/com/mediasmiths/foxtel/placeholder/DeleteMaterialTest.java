package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class DeleteMaterialTest extends PlaceHolderMessageShortTest {

	public DeleteMaterialTest() throws JAXBException, SAXException, IOException {
		super();
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialNotProtected() throws IOException, Exception {
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteMaterialTitleNotProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(false);
		
		assertEquals(MessageValidationResult.IS_VALID,validator.validateFile(temp.getAbsolutePath()));
		
		verify(mayamClient).isTitleOrDescendentsProtected(EXISTING_TITLE);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialIsProtected() throws IOException, Exception {
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(true,PROTECTED_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteMaterialTitleIsProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(PROTECTED_TITLE)).thenReturn(true);
		
		assertEquals(MessageValidationResult.MATERIAL_IS_PROTECTED,validator.validateFile(temp.getAbsolutePath()));
		verify(mayamClient).isTitleOrDescendentsProtected(PROTECTED_TITLE);
	}
	

	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialProtectedCheckFails() throws IOException, Exception {
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteMaterialTitleNotProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
		
		assertEquals(MessageValidationResult.MAYAM_CLIENT_ERROR,validator.validateFile(temp.getAbsolutePath()));
		
		verify(mayamClient).isTitleOrDescendentsProtected(EXISTING_TITLE);
	}
	

	@Test
	@Category(ProcessingTests.class)
	public void testDeleteMaterialProcessing() throws DatatypeConfigurationException, MessageProcessingFailedException{
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), pm);
		
		DeleteMaterial dm = (DeleteMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deleteMaterial(dm)).thenReturn(MayamClientErrorCode.SUCCESS);
		
		//the call we are testing
		processor.processMessage(envelope);
		
		//verify expected calls
		verify(mayamClient).deleteMaterial(dm);
		
		
	}
	
	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testDeleteMaterialProcessingFails() throws DatatypeConfigurationException, MessageProcessingFailedException{
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), pm);
		
		DeleteMaterial dm = (DeleteMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deleteMaterial(dm)).thenReturn(MayamClientErrorCode.MATERIAL_UPDATE_FAILED);
		
		//the call we are testing
		processor.processMessage(envelope);
	}

	private PlaceholderMessage buildDeleteMaterialRequest(boolean materialProtected, String titleID) throws DatatypeConfigurationException {
		
		Material m = new Material();
		m.setMaterialID(EXISTING_MATERIAL_ID);
		
		DeleteMaterial dm = new DeleteMaterial();
		dm.setTitleID(titleID);
		dm.setMaterial(m);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				dm);
		
		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(MESSAGE_ID);
		pm.setSenderID(SENDER_ID);
		pm.setActions(actions);
		return pm;
		
	}

	
}
