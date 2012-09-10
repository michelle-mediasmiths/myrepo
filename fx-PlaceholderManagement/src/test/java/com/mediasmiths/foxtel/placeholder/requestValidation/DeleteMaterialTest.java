package com.mediasmiths.foxtel.placeholder.requestValidation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidationResult;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

public class DeleteMaterialTest extends PlaceHolderMessageValidatorTest {

	public DeleteMaterialTest() throws JAXBException, SAXException {
		super();
	}

	@Test
	public void testDeleteMaterialNotProtected() throws IOException, Exception {
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validDeleteMaterialNotProtected");
		assertEquals(PlaceHolderMessageValidationResult.IS_VALID,toTest.validateFile(temp.getAbsolutePath()));
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
