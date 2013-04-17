package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder;

import com.mediasmiths.foxtel.multiplaceholder.foxtel.messagetests.ResultLogger;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class MultipleActionsInMessageTest_FXT_4_1_0_2 extends PlaceHolderMessageShortTest
{

	private static Logger logger = Logger.getLogger(MultipleActionsInMessageTest_FXT_4_1_0_2.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);
	
	public MultipleActionsInMessageTest_FXT_4_1_0_2() throws JAXBException, SAXException, IOException {
		super();	
	}

	@Test
	public void testMultipleActionsInMessage() throws Exception{
/*
		logger.info("Starting FXT 4.1.0.2 – Multiple Actions in Message");
		
		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);		
		List<Object> createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial = pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial();
		
		PurgeTitle purge = new PurgeTitle();
		purge.setTitleID(EXISTING_TITLE);
		
		createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial.add(purge);
		
		PickupPackage pp = createTempXMLFile (pm,"multipleActions");
		
		//test that the correct validation result is returned
		
		MessageValidationResultPackage<PlaceholderMessage> validationResult= validator.validatePickupPackage(pp);
		if (MessageValidationResult.ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS ==validationResult.getResult())
			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message --Passed");
		else
			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message --Failed");
		
		assertEquals(MessageValidationResult.ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS, validationResult.getResult());
		
		
		Util.deleteFiles(pp); */
	}
	
}
