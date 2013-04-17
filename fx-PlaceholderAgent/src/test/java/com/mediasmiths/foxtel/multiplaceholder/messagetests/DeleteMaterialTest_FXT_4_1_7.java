package com.mediasmiths.foxtel.multiplaceholder.messagetests;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResultPackage;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteMaterialTest_FXT_4_1_7 extends PlaceHolderMessageShortTest{
	private static Logger logger = Logger.getLogger(DeleteMaterialTest_FXT_4_1_7.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);


	public DeleteMaterialTest_FXT_4_1_7() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testDeleteMaterialProcessing() throws DatatypeConfigurationException, MessageProcessingFailedException, Exception {
		
		logger.info("Delete material processing test");
		PlaceholderMessage message = buildDeleteMaterial(false, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), message);
		
		DeleteMaterial dm = (DeleteMaterial) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);

		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		when(mayamClient.materialExists(EXISTING_MATERIAL_ID)).thenReturn(true);


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
		PickupPackage pp = new PickupPackage("xml");
		pp.addPickUp(temp);
		
		IOUtils.write("InvalidDeleteMaterial", new FileOutputStream(temp));
		MessageValidationResultPackage<PlaceholderMessage> validationResult= validator.validatePickupPackage(pp);
		if (MessageValidationResult.FAILS_XSD_CHECK ==validationResult.getResult())
			resultLogger.info("FXT 4.1.7.2 - Non XSD compliance --Passed");
		else
			resultLogger.info("FXT 4.1.7.2 - Non XSD compliance --Failed");
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validationResult.getResult());
		Util.deleteFiles(pp);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialNotProtected_FXT_4_1_7_3_4_5() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.7.3/4/5 - XSD Compliance/ Valid DeleteMaterialMessage/ Matching ID exists");
		PlaceholderMessage message = buildDeleteMaterial(false, EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (message, "validDeleteMaterialNotProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(false);
		when(mayamClient.materialExists(EXISTING_MATERIAL_ID)).thenReturn(true);
		
		MessageValidationResultPackage<PlaceholderMessage> validationResult= validator.validatePickupPackage(pp);
		if (MessageValidationResult.IS_VALID ==validationResult.getResult())
			resultLogger.info("FXT 4.1.7.3/4/5  - XSD Compliance/ Valid DeleteMaterialMessage/ Matching ID exists --Passed");
		else
			resultLogger.info("FXT 4.1.7.3/4/5  - XSD Compliance/ Valid DeleteMaterialMessage/ Matching ID exists --Failed");
		
		assertEquals(MessageValidationResult.IS_VALID, validationResult.getResult());		Util.deleteFiles(pp);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialDoesntExist_FXT_4_1_7_6() throws Exception {
		
		logger.info("Starting FXT 4.1.7.6 - No matching ID");
		
		PlaceholderMessage message = buildDeleteMaterial(false, NOT_EXISTING_MATERIAL);
		PickupPackage pp = createTempXMLFile (message, "deleteMaterialDoesntExist");
		
		when(mayamClient.materialExists(NOT_EXISTING_MATERIAL)).thenReturn(false);

		try
		{
		    validator.validateDeleteMaterial((DeleteMaterial) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
			resultLogger.info("FXT 4.1.7.6 - No matching ID --failed");
			fail();
		}
		catch (MessageValidationException e)
		{
			resultLogger.info("FXT 4.1.7.6 - No matching ID --passed");
		}
		
		Util.deleteFiles(pp);
	}
	
	@Test
	@Category (ValidationTests.class)
	public void testDeleteMaterialProtected_FXT_4_1_7_7() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.7.7 - Material is protected");
		PlaceholderMessage message = buildDeleteMaterial(true, PROTECTED_TITLE);
		PickupPackage pp = createTempXMLFile (message, "validDeleteMaterialProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(PROTECTED_TITLE)).thenReturn(true);

		try
		{
		   validator.validateDeleteMaterial((DeleteMaterial) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
		   resultLogger.info("FXT 4.1.7.7 - Material is protected --Failed");
		   fail();
		}
		catch (MessageValidationException e)
		{
			assertEquals(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED, e.result);
		}


		Util.deleteFiles(pp);
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
		message.setMessageID(createMessageID());
		message.setSenderID(createSenderID());
		message.setActions(actions);
		
		return message;
	}

}
