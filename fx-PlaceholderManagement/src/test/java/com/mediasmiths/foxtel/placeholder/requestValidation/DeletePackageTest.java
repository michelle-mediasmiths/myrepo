package com.mediasmiths.foxtel.placeholder.requestValidation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.Package;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class DeletePackageTest extends PlaceHolderMessageValidatorTest {

	public DeletePackageTest() throws JAXBException, SAXException {
		super();
	}
	
	@Test
	public void testDeletePackageNotProtected() throws IOException, Exception {
		
		PlaceholderMessage pm = buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		File temp = createTempXMLFile(pm, "validDeletePackageMaterialNotProtected");
		
		when(mayamClient.isMaterialForPackageProtected(EXISTING_PACKAGE_ID)).thenReturn(false);
		
		assertEquals(MessageValidationResult.IS_VALID,toTest.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	public void testDeletePackageIsProected() throws IOException, Exception {
		PlaceholderMessage pm = buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		File temp = createTempXMLFile(pm, "validDeletePackageMaterialProtected");
		
		when(mayamClient.isMaterialForPackageProtected(EXISTING_PACKAGE_ID)).thenReturn(true);
		
		assertEquals(MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED,toTest.validateFile(temp.getAbsolutePath()));
	}

	@Test(expected = MayamClientException.class)
	public void testDeletePackageRequestFail() throws IOException, Exception{
		PlaceholderMessage pm = buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		File temp = createTempXMLFile(pm, "validDeletePackageRequestFailure");
		
		when(mayamClient.isMaterialForPackageProtected(EXISTING_PACKAGE_ID)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
		
		toTest.validateFile(temp.getAbsolutePath());
	}
	
	private PlaceholderMessage buildDeletePackageRequest(boolean b,
			String titleID, String packageID) {
		
		Package p = new Package();
		p.setPresentationID(packageID);
		
		DeletePackage dp = new DeletePackage();
		dp.setTitleID(titleID);
		dp.setPackage(p);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				dp);
		
		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(MESSAGE_ID);
		pm.setSenderID(SENDER_ID);
		pm.setActions(actions);
		return pm;
	}
}
