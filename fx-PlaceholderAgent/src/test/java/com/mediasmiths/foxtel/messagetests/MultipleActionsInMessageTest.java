package com.mediasmiths.foxtel.messagetests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;

public class MultipleActionsInMessageTest extends PlaceHolderMessageShortTest{

	public MultipleActionsInMessageTest() throws JAXBException, SAXException, IOException {
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
		assertEquals(MessageValidationResult.ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS,validator.validateFile(temp.getAbsolutePath()));
	}
	
}
