package com.mediasmiths.foxtel.placeholder.validation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;

public class MultipleActionsInMessageTest extends PlaceHolderMessageValidatorTest{

	public MultipleActionsInMessageTest() throws JAXBException, SAXException {
		super();	
	}

	@Test
	public void testMultipleActionsInMessage() throws Exception{

		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);		
		List<Object> createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial = pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial();
		
		PurgeTitle purge = new PurgeTitle();
		purge.setTitleID(EXISTING_TITLE);
		
		createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial.add(purge);
		
		File temp = createTempXMLFile(pm,"multipleActions");
		
		//test that the correct validation result is returned
		assertEquals(MessageValidationResult.ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS,toTest.validateFile(temp.getAbsolutePath()));
	}
	
}
