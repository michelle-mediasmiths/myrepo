package com.mediasmiths.foxtel.multiplaceholder.foxtel.messagetests;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.ClassificationEnumType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResultPackage;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class AddOrUpdatePackageTest_FXT_4_1_9 extends PlaceHolderMessageShortTest
{
	
	protected final static String EXISTING_TITLE = "EXISTING_TITLE";
	protected final static String EXISTING_MATERIAL = "EXISTING_MATERIAL";
	protected final static String NOT_EXISTING_MATERIAL = "NOT_SXISTING_MATERIAL";
	
	protected final static String NEW_PACKAGE = "NEW_PACKAGE";
	private static Logger logger = Logger.getLogger(AddOrUpdatePackageTest_FXT_4_1_9.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);


	public AddOrUpdatePackageTest_FXT_4_1_9() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testAddPackageProcessing() throws Exception {
		
		logger.info("Add package processing test");
		PlaceholderMessage message = buildAddPackage(NEW_PACKAGE, EXISTING_MATERIAL, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), message);
		
		AddOrUpdatePackage aoup = (AddOrUpdatePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);

		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(new Boolean(true));
		when(mayamClient.materialExists(EXISTING_MATERIAL_ID)).thenReturn(new Boolean(true));


		when(mayamClient.packageExists(NEW_PACKAGE)).thenReturn(new Boolean(false));
		when(mayamClient.createPackage(aoup.getPackage())).thenReturn(MayamClientErrorCode.SUCCESS);
		processor.processMessage(envelope);
		verify(mayamClient).createPackage(aoup.getPackage());
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testAddPackageXSDInvalid_FXT_4_1_9_2() throws Exception {
		
		logger.info("Starting FXT 4.1.9.2 - Non XSD compliance");
		//File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		File temp = new File("/tmp/placeHolderTestData/NonXSDConformingFile__"+RandomStringUtils.randomAlphabetic(6)+ ".xml");
		temp.createNewFile();
		PickupPackage pp = new PickupPackage("xml");
		pp.addPickUp(temp);
		
		IOUtils.write("InvalidAddPackage", new FileOutputStream(temp));
		
		MessageValidationResultPackage<PlaceholderMessage> validationResult = validator.validatePickupPackage(pp);
		if (MessageValidationResult.FAILS_XSD_CHECK ==validationResult.getResult())
			resultLogger.info("FXT 4.1.9.2 - Non XSD compliance --Passed");
		else
			resultLogger.info("FXT 4.1.9.2 - Non XSD compliance --Failed");
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validationResult.getResult());
		Util.deleteFiles(pp);
	}
	
	@Test
	@Category (ValidationTests.class)
	public void testAddValidPackage_FXT_4_1_9_3_4_5() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.9.3/4/5 - XSD Compliance/ Valid AddOrUpdatePackage message/ No matching ID exists");
		PlaceholderMessage message = buildAddPackage(NEW_PACKAGE, EXISTING_MATERIAL, EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (message, "validAddPackage");
		
		when(mayamClient.materialExists(EXISTING_MATERIAL)).thenReturn(new Boolean(true));

		try
		{
			validator.validateAddOrUpdatePackage((AddOrUpdatePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));

			resultLogger.info("FXT 4.1.9.3/4/5 - XSD Compliance/ Valid AddOrUpdatePackage message/ No matching ID exists --Passed");

		}
		catch (MessageValidationException e)
		{
			resultLogger.info("FXT 4.1.9.3/4/5 - XSD Compliance/ Valid AddOrUpdatePackage message/ No matching ID exists --Failed");
			fail();
		}
		
		Util.deleteFiles(pp);
	}

	@Test
	@Category(ProcessingTests.class)
	public void testUpdatePackageProcessing_FXT_4_1_9_6() throws Exception {
		
		logger.info("Starting FXT 4.1.9.6 - Matching ID exists");
		PlaceholderMessage message = buildAddPackage(EXISTING_PACKAGE_ID, EXISTING_MATERIAL, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), message);
		
		AddOrUpdatePackage aoup = (AddOrUpdatePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);

		when(mayamClient.materialExists(EXISTING_MATERIAL)).thenReturn(new Boolean (true));
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(new Boolean (true));
		when(mayamClient.packageExists(EXISTING_PACKAGE_ID)).thenReturn(new Boolean (true));
		when(mayamClient.updatePackage(aoup.getPackage())).thenReturn(MayamClientErrorCode.SUCCESS);
		try{
			processor.processMessage(envelope);
		}
		catch (MessageProcessingFailedException e)
		{
			resultLogger.info("FXT 4.1.9.6 - Matching ID exists --Passed");
			throw e;
		}
		
		verify(mayamClient).updatePackage(aoup.getPackage());
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testAddPackageNoMaterial_FXT_4_1_9_7() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.9.7 - No existing material");
		PlaceholderMessage message = buildAddPackage(NEW_PACKAGE, NOT_EXISTING_MATERIAL, EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (message, "addPackageNoMaterial");

		when(mayamClient.materialExists(EXISTING_MATERIAL)).thenReturn(new Boolean (true));
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(new Boolean (true));
		when(mayamClient.materialExists(NOT_EXISTING_MATERIAL)).thenReturn(new Boolean(false));

		try
		{
		   validator.validateAddOrUpdatePackage((AddOrUpdatePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
		   resultLogger.info("FXT 4.1.9.7 - No existing material --Passed");
		}
		catch (MessageValidationException e)
		{
			resultLogger.info("FXT 4.1.9.7 - No existing material --Failed");
		}
		
		Util.deleteFiles(pp);
		}
	
	public PlaceholderMessage buildAddPackage (String packageID, String materialID, String titleID) throws DatatypeConfigurationException {
		
		PackageType pack = new PackageType();
		pack = buildPackage(pack, packageID, materialID);
		
		AddOrUpdatePackage aoup = new AddOrUpdatePackage();
		aoup.setTitleID(titleID);
		aoup.setPackage(pack);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(aoup);
		
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID(createMessageID());
		message.setSenderID(createSenderID());
		message.setActions(actions);
		
		return message;
	}
	
	public PackageType buildPackage (PackageType pack, String packageID, String materialID) throws DatatypeConfigurationException {
		
		pack.setMaterialID(materialID);
		PresentationFormatType format = buildPresentationFormat("SD");
		pack.setPresentationFormat(format);
		ClassificationEnumType classification = buildClassification("PG");
		pack.setClassification(classification);
		pack.setConsumerAdvice("Generally suitable for all");
		pack.setNumberOfSegments(new BigInteger("4"));
		HelperMethods helper = new HelperMethods();
		XMLGregorianCalendar xmlCal = helper.giveValidDate();
		pack.setTargetDate(xmlCal);
		pack.setNotes("none");
		pack.setPresentationID(packageID);
		
		return pack;
	}
	
	public PresentationFormatType buildPresentationFormat (String v) {
		
		PresentationFormatType format = null;
		format = PresentationFormatType.fromValue(v);
		return format;
	}
	
	public ClassificationEnumType buildClassification (String v) {
		ClassificationEnumType classification = null;
		classification = ClassificationEnumType.fromValue(v);
		return classification;
	}

}
