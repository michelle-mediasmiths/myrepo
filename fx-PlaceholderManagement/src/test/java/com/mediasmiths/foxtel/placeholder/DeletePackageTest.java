package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.Package;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class DeletePackageTest extends PlaceHolderMessageValidatorTest {

	public DeletePackageTest() throws JAXBException, SAXException {
		super();
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageNotProtected() throws IOException, Exception {
		
		PlaceholderMessage pm = buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		File temp = createTempXMLFile(pm, "validDeletePackageMaterialNotProtected");
		
		when(mayamClient.isMaterialForPackageProtected(EXISTING_PACKAGE_ID)).thenReturn(false);
		
		assertEquals(MessageValidationResult.IS_VALID,validator.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageIsProected() throws IOException, Exception {
		PlaceholderMessage pm = buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		File temp = createTempXMLFile(pm, "validDeletePackageMaterialProtected");
		
		when(mayamClient.isMaterialForPackageProtected(EXISTING_PACKAGE_ID)).thenReturn(true);
		
		assertEquals(MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED,validator.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testDeletePackageProcessing() throws MessageProcessingFailedException{
		
		PlaceholderMessage pm =buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		
		DeletePackage dp = (DeletePackage) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deletePackage(dp)).thenReturn(MayamClientErrorCode.SUCCESS);
		
		//the call we are testing
		processor.processPlaceholderMesage(pm);
		
		//verify expected calls
		verify(mayamClient).deletePackage(dp);
		
		
	}
	
	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testDeletePackageProcessingFails() throws MessageProcessingFailedException{
		
	PlaceholderMessage pm =buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		
		DeletePackage dp = (DeletePackage) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deletePackage(dp)).thenReturn(MayamClientErrorCode.PACKAGE_UPDATE_FAILED);
		
		//the call we are testing
		processor.processPlaceholderMesage(pm);		
	}


	@Test(expected = MayamClientException.class)
	@Category(ValidationTests.class)
	public void testDeletePackageRequestFail() throws IOException, Exception{
		PlaceholderMessage pm = buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		File temp = createTempXMLFile(pm, "validDeletePackageRequestFailure");
		
		when(mayamClient.isMaterialForPackageProtected(EXISTING_PACKAGE_ID)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
		
		validator.validateFile(temp.getAbsolutePath());
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
