package com.mediasmiths.foxtel.messagetests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResultPackage;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.placeholder.util.Util;

public class MultipleActionsInMessageTest extends PlaceHolderMessageShortTest{

	private static Logger logger = Logger.getLogger(MultipleActionsInMessageTest.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);
	
	public MultipleActionsInMessageTest() throws JAXBException, SAXException, IOException {
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
		
		PickupPackage pp = createTempXMLFile (pm,"multipleActions");
		
		//test that the correct validation result is returned
		MessageValidationResultPackage<PlaceholderMessage> validationResult=	 validator.validatePickupPackage(pp);
		if (MessageValidationResult.ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS ==validationResult.getResult())
			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message --Passed");
		else
			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message--Failed");
		
		assertEquals(MessageValidationResult.ACTIONS_ELEMENT_CONTAINED_MUTIPLE_ACTIONS, validationResult.getResult());
		Util.deleteFiles(pp);
	}
	
}
