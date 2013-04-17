package com.mediasmiths.foxtel.multiplaceholder.messagetests;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.Package;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.SegmentList;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class DeletePackageTest_FXT_4_1_11 extends PlaceHolderMessageShortTest {
	
	private static Logger logger = Logger.getLogger(DeletePackageTest_FXT_4_1_11.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	
	public DeletePackageTest_FXT_4_1_11() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testDeletePackageProcessing() throws MessageProcessingFailedException {
		
		logger.info ("Delete package processing test");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, EXISTING_PACKAGE_ID);
		
		DeletePackage dp = (DeletePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		when(mayamClient.deletePackage(dp)).thenReturn(MayamClientErrorCode.SUCCESS);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageXSDInvalid_FXT_4_1_11_2() throws Exception {
		
		logger.info("Starting FXT 4.1.11.2 - Non XSD compliance");
		//File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		File temp = new File("/tmp/placeHolderTestData/NonXSDConformingFile__"+RandomStringUtils.randomAlphabetic(6)+ ".xml");
		
		PickupPackage pp = new PickupPackage("xml");
		pp.addPickUp(temp);
		
		IOUtils.write("InvalidDeletePackage", new FileOutputStream(temp));
		MessageValidationResultPackage<PlaceholderMessage> validationResult= validator.validatePickupPackage(pp);
		if (MessageValidationResult.FAILS_XSD_CHECK ==validationResult.getResult())
			resultLogger.info("FXT 4.1.11.2 - Non XSD compliance --Passed");
		else
			resultLogger.info("FXT 4.1.11.2 - Non XSD compliance --Failed");
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validationResult.getResult());	
		Util.deleteFiles(pp);
		
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageNotProtected_FXT_4_1_11_3_4_5 () throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.11.3/4/5 - XSD Compliance/ Valid DeletePackage message/ Matching ID exists");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, EXISTING_PACKAGE_ID);
		PickupPackage pp = createTempXMLFile (message, "validDeletePackageNotProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(new Boolean(false));
		when(mayamClient.packageExists(EXISTING_PACKAGE_ID)).thenReturn(true);
		
		SegmentList sl = new SegmentList();
		AttributeMap am = new AttributeMap();
		am.setAttribute(Attribute.PARENT_HOUSE_ID,EXISTING_MATERIAL_ID);
		sl.setAttributeMap(am);
		
		when(mayamClient.getTxPackage(EXISTING_PACKAGE_ID)).thenReturn(sl);
		
		MessageValidationResultPackage<PlaceholderMessage> validationResult= validator.validatePickupPackage(pp);

		if (MessageValidationResult.IS_VALID ==validationResult.getResult())
			resultLogger.info("FXT  4.1.11.3/4/5 - XSD Compliance/ Valid DeletePackage message/ Matching ID exists--Passed");
		else
			resultLogger.info("FXT  4.1.11.3/4/5 - XSD Compliance/ Valid DeletePackage message/ Matching ID exists --Failed");
		
		assertEquals(MessageValidationResult.IS_VALID, validationResult.getResult());	
		Util.deleteFiles(pp);
	}
	
	@Test
	@Category (ValidationTests.class) 
	public void testDeletePackageDoesntExist_FXT_4_1_11_6() throws Exception {
		
		logger.info("Starting FXT 4.1.11.6 - No matching ID");
		
		PlaceholderMessage message = buildDeletePackage(false, EXISTING_TITLE, NOT_EXISTING_PACKAGE);
		PickupPackage pp = createTempXMLFile (message, "deletePackageDoesntExist");
		
		when(mayamClient.packageExists(NOT_EXISTING_PACKAGE)).thenReturn(false);
		
		try
		{
			validator.validateDeletePackage((DeletePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
			resultLogger.info("FXT 4.1.11.6 - No matching ID --Failed");


		}
		catch (MessageValidationException e)
		{
			resultLogger.info("FXT 4.1.11.6 - No matching ID --Passed");
			assertEquals(MessageValidationResult.PACKAGE_DOES_NOT_EXIST, e.result);

		}

		Util.deleteFiles(pp);
		
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageProtected_FXT_4_1_11_7 () throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.11.7 - Package is protected");
		
		PlaceholderMessage message = buildDeletePackage(false, PROTECTED_TITLE, PROTECTED_PACKAGE);
		PickupPackage pp = createTempXMLFile (message, "validDeletePackageProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(PROTECTED_TITLE)).thenReturn(true);

		try
		{
		   validator.validateDeletePackage((DeletePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
		   fail();

		}
		catch (MessageValidationException e)
		{
			resultLogger.info("FXT 4.1.11.7 - Package is protected --Passed");
			assertEquals(MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED, e.result);

		}

		
		Util.deleteFiles(pp);
	}

	public PlaceholderMessage buildDeletePackage (boolean b, String titleID, String packageID) {
		
		Package pack = new Package();
		pack.setPresentationID(packageID);
		
		DeletePackage dp = new DeletePackage();
		dp.setTitleID(titleID);
		dp.setPackage(pack);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(dp);
		
		PlaceholderMessage message  = new PlaceholderMessage();
		message.setMessageID(createMessageID());
		message.setSenderID(createSenderID());
		message.setActions(actions);
		return message;
	}
}
