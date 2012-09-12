package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;
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
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class DeleteMaterialTest extends PlaceHolderMessageValidatorTest {

	public DeleteMaterialTest() throws JAXBException, SAXException {
		super();
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialNotProtected() throws IOException, Exception {
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteMaterialTitleNotProtected");
		assertEquals(MessageValidationResult.IS_VALID,validator.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testDeleteMaterialProcessing() throws DatatypeConfigurationException, MessageProcessingFailedException{
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		
		DeleteMaterial dm = (DeleteMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deleteMaterial(dm)).thenReturn(MayamClientErrorCode.SUCCESS);
		
		//the call we are testing
		processor.processPlaceholderMesage(pm);
		
		//verify expected calls
		verify(mayamClient).deleteMaterial(dm);
		
		
	}
	
	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testDeleteMaterialProcessingFails() throws DatatypeConfigurationException, MessageProcessingFailedException{
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		
		DeleteMaterial dm = (DeleteMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deleteMaterial(dm)).thenReturn(MayamClientErrorCode.MATERIAL_UPDATE_FAILED);
		
		//the call we are testing
		processor.processPlaceholderMesage(pm);
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
