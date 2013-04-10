package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class DeletePackageTest extends PlaceHolderMessageShortTest {

	public DeletePackageTest() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageNotProtected() throws IOException, Exception {
		
		PlaceholderMessage pm = buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		PickupPackage pp = createTempXMLFile (pm, "validDeletePackageMaterialNotProtected");
		
		when(mayamClient.packageExists(EXISTING_PACKAGE_ID)).thenReturn(true);
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_PACKAGE_ID)).thenReturn(false);
		
		assertEquals(MessageValidationResult.IS_VALID,validator.validatePickupPackage(pp).getResult());
		verify(mayamClient).isTitleOrDescendentsProtected(EXISTING_TITLE);

		Util.deleteFiles(pp);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageIsProected() throws IOException, Exception {
		PlaceholderMessage pm = buildDeletePackageRequest(false,PROTECTED_TITLE,PROTECTED_PACKAGE); 
		PickupPackage pp = createTempXMLFile (pm, "validDeletePackageMaterialProtected");
		
		when(mayamClient.packageExists(PROTECTED_PACKAGE)).thenReturn(true);
		when(mayamClient.isTitleOrDescendentsProtected(PROTECTED_TITLE)).thenReturn(true);

		assertEquals(MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED,validator.validatePickupPackage(pp).getResult());
		verify(mayamClient).isTitleOrDescendentsProtected(PROTECTED_TITLE);

		Util.deleteFiles(pp);
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testDeletePackageProcessing() throws MessageProcessingFailedException{
		
		PlaceholderMessage pm =buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage(), pm);
		
		DeletePackage dp = (DeletePackage) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deletePackage(dp)).thenReturn(MayamClientErrorCode.SUCCESS);
		
		//the call we are testing
		processor.processMessage(envelope);
		
		//verify expected calls
		verify(mayamClient).deletePackage(dp);
		
		
	}
	
	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testDeletePackageProcessingFails() throws MessageProcessingFailedException{
		
	PlaceholderMessage pm =buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
	MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage(), pm);	
	
		DeletePackage dp = (DeletePackage) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deletePackage(dp)).thenReturn(MayamClientErrorCode.PACKAGE_UPDATE_FAILED);
		
		//the call we are testing
		processor.processMessage(envelope);		
	}


	@Test
	@Category(ValidationTests.class)
	public void testDeletePackageRequestFail() throws IOException, Exception{
		PlaceholderMessage pm = buildDeletePackageRequest(false,EXISTING_TITLE,ERROR_PACKAGE_ID);
		PickupPackage pp = createTempXMLFile (pm, "validDeletePackageRequestFailure");
		
		when(mayamClient.isTitleOrDescendentsProtected(ERROR_TITLE_ID)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
		
		// try to call validation, expect a mayam client error
		assertEquals(
				MessageValidationResult.MAYAM_CLIENT_ERROR,
				validator.validatePickupPackage(pp).getResult());
		Util.deleteFiles(pp);
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
		pm.setMessageID(createMessageID());
		pm.setSenderID(createSenderID());
		pm.setActions(actions);
		return pm;
	}
}
