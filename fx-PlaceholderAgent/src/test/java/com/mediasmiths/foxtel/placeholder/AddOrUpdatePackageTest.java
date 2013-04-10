package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import javax.xml.bind.JAXBException;

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
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class AddOrUpdatePackageTest extends PlaceHolderMessageShortTest {

	protected final static String EXISTING_TITLE = "EXISTING_TITLE";
	protected final static String EXISTING_MATERIAL = "EXISTING_MATERIAL";
	protected final static String NOT_EXISTING_MATERIAL = "NOT_EXISTING_MATERIAL";
	
	protected final static String NEW_PACKAGE = "NEW_PACKAGE";

	public AddOrUpdatePackageTest() throws JAXBException, SAXException, IOException {
		super();
	}

	@Test
	@Category(ValidationTests.class)
	public void testAddValidPackage() throws IOException, Exception {
		PlaceholderMessage pm = buildCreatePackage(NEW_PACKAGE,
				EXISTING_MATERIAL,EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (pm, "validAddPackage");

		when(mayamClient.materialExists(EXISTING_MATERIAL)).thenReturn(
				new Boolean(true));

		// test that the generated placeholder message is valid
		assertEquals(MessageValidationResult.IS_VALID,
				validator.validatePickupPackage(pp));
		
		Util.deleteFiles(pp);
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testValidAddPackageProcessing() throws Exception {


		PlaceholderMessage pm = buildCreatePackage(NEW_PACKAGE,
				EXISTING_MATERIAL,EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		
		AddOrUpdatePackage aoup = (AddOrUpdatePackage) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		//prepare mock mayamClient
		when(mayamClient.packageExists(NEW_PACKAGE)).thenReturn(
				new Boolean(false));
		when(mayamClient.createPackage(aoup.getPackage())).thenReturn(MayamClientErrorCode.SUCCESS);
		//the call we are testing
		processor.processMessage(envelope);
		//verfiy update call took place
		verify(mayamClient).createPackage(aoup.getPackage());
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testValidUpdatePackageProcessing() throws Exception {

		PlaceholderMessage pm = buildCreatePackage(EXISTING_PACKAGE_ID,
				EXISTING_MATERIAL,EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		
		AddOrUpdatePackage aoup = (AddOrUpdatePackage) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		//prepare mock mayamClient
		when(mayamClient.packageExists(EXISTING_PACKAGE_ID)).thenReturn(
				new Boolean(true));
		when(mayamClient.updatePackage(aoup.getPackage())).thenReturn(MayamClientErrorCode.SUCCESS);
		//the call we are testing
		processor.processMessage(envelope);
		//verfiy update call took place
		verify(mayamClient).updatePackage(aoup.getPackage());

		
	}

	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testValidAddPackageProcessingFailsOnQueryingExistingPackage() throws Exception {

		PlaceholderMessage pm = buildCreatePackage(NEW_PACKAGE,
				EXISTING_MATERIAL,EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		
		//prepare mock mayamClient
		when(mayamClient.packageExists(NEW_PACKAGE)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
		//the call we are testing
		processor.processMessage(envelope);
		
	}
	
	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testValidAddPackageProcessingFailesOnCreatePackage() throws Exception {

		PlaceholderMessage pm = buildCreatePackage(NEW_PACKAGE,
				EXISTING_MATERIAL,EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		
		AddOrUpdatePackage aoup = (AddOrUpdatePackage) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		//prepare mock mayamClient
		when(mayamClient.packageExists(NEW_PACKAGE)).thenReturn(
				new Boolean(false));
		when(mayamClient.createPackage(aoup.getPackage())).thenReturn(MayamClientErrorCode.PACKAGE_CREATION_FAILED);
		//the call we are testing
		processor.processMessage(envelope);
				
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testAddPackageRequestFails() throws IOException, Exception {
		PlaceholderMessage pm = buildCreatePackage(NEW_PACKAGE,
				EXISTING_MATERIAL,EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (pm, "validAddPackage");

		when(mayamClient.materialExists(EXISTING_MATERIAL)).thenThrow(
				new MayamClientException(MayamClientErrorCode.FAILURE));

		// try to call validation, expect a mayam client error
		assertEquals(
				MessageValidationResult.MAYAM_CLIENT_ERROR,
				validator.validatePickupPackage(pp));
		
		Util.deleteFiles(pp);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testAddPackageInvalidMaterial() throws IOException, Exception{
		PlaceholderMessage pm = buildCreatePackage(NEW_PACKAGE,
				NOT_EXISTING_MATERIAL,EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (pm, "addPackageInvalidMaterialID");

		when(mayamClient.materialExists(NOT_EXISTING_MATERIAL)).thenReturn(
				new Boolean(false));

		// test that the validation result is correct
		assertEquals(MessageValidationResult.NO_EXISTING_MATERIAL_FOR_PACKAGE,
				validator.validatePickupPackage(pp));
		
		Util.deleteFiles(pp);
	}

	private PlaceholderMessage buildCreatePackage(String packageid,
			String materialid, String titleid) {
				
		PackageType pack = new PackageType();
		pack.setPresentationID(packageid);		
		pack.setMaterialID(materialid);
		pack.setPresentationFormat(PresentationFormatType.HD);
		pack.setClassification(ClassificationEnumType.G);
		pack.setConsumerAdvice("L");
		pack.setNumberOfSegments(new BigInteger("1"));
		
		
		AddOrUpdatePackage aup = new AddOrUpdatePackage();
		aup.setTitleID(titleid);
		aup.setPackage(pack);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				aup);
		
		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(createMessageID());
		pm.setSenderID(createSenderID());
		pm.setActions(actions);
		return pm;
	}

}
