package com.mediasmiths.foxtel.placeholder.validation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.ClassificationEnumType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class AddOrUpdatePackageTest extends PlaceHolderMessageValidatorTest {

	protected final static String EXISTING_TITLE = "EXISTING_TITLE";
	protected final static String EXISTING_MATERIAL = "EXISTING_MATERIAL";
	protected final static String NOT_EXISTING_MATERIAL = "NOT_EXISTING_MATERIAL";
	
	protected final static String NEW_PACKAGE = "NEW_PACKAGE";

	public AddOrUpdatePackageTest() throws JAXBException, SAXException {
		super();
	}

	@Test
	public void testAddValidPackage() throws IOException, Exception {
		PlaceholderMessage pm = buildCreatePackage(NEW_PACKAGE,
				EXISTING_MATERIAL,EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validAddPackage");

		when(mayamClient.materialExists(EXISTING_MATERIAL)).thenReturn(
				new Boolean(true));

		// test that the generated placeholder message is valid
		assertEquals(MessageValidationResult.IS_VALID,
				toTest.validateFile(temp.getAbsolutePath()));
	}
	
	@Test(expected = MayamClientException.class)
	public void testAddPackageRequestFails() throws IOException, Exception {
		PlaceholderMessage pm = buildCreatePackage(NEW_PACKAGE,
				EXISTING_MATERIAL,EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validAddPackage");

		when(mayamClient.materialExists(EXISTING_MATERIAL)).thenThrow(
				new MayamClientException(MayamClientErrorCode.FAILURE));

		// try to call validation, expect an exception
		toTest.validateFile(temp.getAbsolutePath());
	}
	
	@Test
	public void testAddPackageInvalidMaterial() throws IOException, Exception{
		PlaceholderMessage pm = buildCreatePackage(NEW_PACKAGE,
				NOT_EXISTING_MATERIAL,EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "addPackageInvalidMaterialID");

		when(mayamClient.materialExists(NOT_EXISTING_MATERIAL)).thenReturn(
				new Boolean(false));

		// test that the validation result is correct
		assertEquals(MessageValidationResult.NO_EXISTING_MATERIAL_FOR_PACKAGE,
				toTest.validateFile(temp.getAbsolutePath()));
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
		pm.setMessageID(MESSAGE_ID);
		pm.setSenderID(SENDER_ID);
		pm.setActions(actions);
		return pm;
	}

}
