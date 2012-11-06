package com.mediasmiths.foxtel.messagetests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.ClassificationEnumType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;


public class AddOrUpdatePackageTest extends PlaceHolderMessageShortTest {
	
	protected final static String EXISTING_TITLE = "EXISTING_TITLE";
	protected final static String EXISTING_MATERIAL = "EXISTING_MATERIAL";
	protected final static String NOT_EXISTING_MATERIAL = "NOT_SXISTING_MATERIAL";
	
	protected final static String NEW_PACKAGE = "NEW_PACKAGE";
	private static Logger logger = Logger.getLogger(AddOrUpdatePackageTest.class);

	public AddOrUpdatePackageTest() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testAddPackageProcessing() throws Exception {
		
		logger.info("Add package processing test");
		PlaceholderMessage message = buildAddPackage(NEW_PACKAGE, EXISTING_MATERIAL, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		AddOrUpdatePackage aoup = (AddOrUpdatePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		when(mayamClient.packageExists(NEW_PACKAGE)).thenReturn(new Boolean(false));
		when(mayamClient.createPackage(aoup.getPackage())).thenReturn(MayamClientErrorCode.SUCCESS);
		processor.processMessage(envelope);
		verify(mayamClient).createPackage(aoup.getPackage());
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testAddPackageXSDInvalid() throws Exception {
		
		logger.info("Starting FXT 4.1.9.2 - Non XSD compliance");
		//File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		File temp = new File("/tmp/placeHolderTestData/NonXSDConformingFile__"+RandomStringUtils.randomAlphabetic(6)+ ".xml");

		IOUtils.write("InvalidAddPackage", new FileOutputStream(temp));
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.FAILS_XSD_CHECK ==validateFile)
			logger.info("FXT 4.1.9.2 - Non XSD compliance --Passed");
		else
			logger.info("FXT 4.1.9.2 - Non XSD compliance --Failed");
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);			
		Util.deleteFiles(temp.getAbsolutePath());
	}
	
	@Test
	@Category (ValidationTests.class)
	public void testAddValidPackage() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.9.3/4/5 - XSD Compliance/ Valid AddOrUpdatePackage message/ No matching ID exists");
		PlaceholderMessage message = buildAddPackage(NEW_PACKAGE, EXISTING_MATERIAL, EXISTING_TITLE);
		File temp = createTempXMLFile(message, "validAddPackage");
		
		when(mayamClient.materialExists(EXISTING_MATERIAL)).thenReturn(new Boolean(true));
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.IS_VALID ==validateFile)
			logger.info("FXT 4.1.9.3/4/5 - XSD Compliance/ Valid AddOrUpdatePackage message/ No matching ID exists --Passed");
		else
			logger.info("FXT 4.1.9.3/4/5 - XSD Compliance/ Valid AddOrUpdatePackage message/ No matching ID exists --Failed");
		
		assertEquals(MessageValidationResult.IS_VALID, validateFile);			
		Util.deleteFiles(temp.getAbsolutePath());
	}

	@Test
	@Category(ProcessingTests.class)
	public void testUpdatePackageProcessing() throws Exception {
		
		logger.info("Starting FXT 4.1.9.6 - Matching ID exists");
		PlaceholderMessage message = buildAddPackage(EXISTING_PACKAGE_ID, EXISTING_MATERIAL, EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		AddOrUpdatePackage aoup = (AddOrUpdatePackage) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		when(mayamClient.packageExists(EXISTING_PACKAGE_ID)).thenReturn(new Boolean (true));
		when(mayamClient.updatePackage(aoup.getPackage())).thenReturn(MayamClientErrorCode.SUCCESS);
		processor.processMessage(envelope);
		verify(mayamClient).updatePackage(aoup.getPackage());
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testAddPackageNoMaterial() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.9.7 - No existing material");
		PlaceholderMessage message = buildAddPackage(NEW_PACKAGE, NOT_EXISTING_MATERIAL, EXISTING_TITLE);
		File temp = createTempXMLFile(message, "addPackageNoMaterial");
		
		when(mayamClient.materialExists(NOT_EXISTING_MATERIAL)).thenReturn(new Boolean(false));
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.NO_EXISTING_MATERIAL_FOR_PACKAGE ==validateFile)
			logger.info("FXT 4.1.9.7 - No existing material --Passed");
		else
			logger.info("FXT 4.1.9.7 - No existing material --Failed");
		
		assertEquals(MessageValidationResult.NO_EXISTING_MATERIAL_FOR_PACKAGE, validateFile);	
		Util.deleteFiles(temp.getAbsolutePath());
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
		message.setMessageID(MESSAGE_ID);
		message.setSenderID(SENDER_ID);
		message.setActions(actions);
		
		return message;
	}
	
	public PackageType buildPackage (PackageType pack, String packageID, String materialID) throws DatatypeConfigurationException {
		
		Random randomGenerator = new Random();
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
		format = format.fromValue(v);
		return format;
	}
	
	public ClassificationEnumType buildClassification (String v) {
		ClassificationEnumType classification = null;
		classification = ClassificationEnumType.fromValue(v);
		return classification;
	}

}
