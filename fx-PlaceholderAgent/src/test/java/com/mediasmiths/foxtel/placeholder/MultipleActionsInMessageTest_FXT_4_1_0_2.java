package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.messagetests.ResultLogger;
import com.mediasmiths.foxtel.placeholder.util.Util;

public class MultipleActionsInMessageTest_FXT_4_1_0_2 extends PlaceHolderMessageShortTest{

	private static Logger logger = Logger.getLogger(MultipleActionsInMessageTest_FXT_4_1_0_2.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);
	
	public MultipleActionsInMessageTest_FXT_4_1_0_2() throws JAXBException, SAXException, IOException {
		super();	
	}

	@Test
	public void testMultipleActionsInMessage() throws Exception{

		logger.info("Starting FXT 4.1.0.2 – Multiple Actions in Message");
		
		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);		
		List<Object> createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial = pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial();
		
		PurgeTitle purge = new PurgeTitle();
		purge.setTitleID(EXISTING_TITLE);
		
		createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial.add(purge);
		
		File temp = createTempXMLFile(pm,"multipleActions");
		
		//test that the correct validation result is returned
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS ==validateFile)
			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message --Passed");
		else
			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message --Failed");
		
		assertEquals(MessageValidationResult.ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS, validateFile);
		
		
		Util.deleteFiles(temp.getAbsolutePath());
	}
	
}
