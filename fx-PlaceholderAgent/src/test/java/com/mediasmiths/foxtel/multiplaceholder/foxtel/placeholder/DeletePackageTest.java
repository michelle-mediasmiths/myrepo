package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder;

import org.junit.Ignore;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;

@Ignore
public class DeletePackageTest extends PlaceHolderMessageShortTest
{

	public DeletePackageTest() throws JAXBException, SAXException, IOException {
		super();
	}
/*
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
		when(mayamClient.packageExists(PROTECTED_TITLE)).thenReturn(true);

		when(mayamClient.isTitleOrDescendentsProtected(PROTECTED_TITLE)).thenReturn(true);

		try
		{
			validator.validateDeletePackage((DeletePackage) pm.getActions()
			                                                  .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
			                                                  .get(0));
		}
		catch (MessageValidationException e)
		{
			assertEquals(MessageValidationResult.PACKAGES_MATERIAL_IS_PROTECTED, e.result);
		}
		;
		verify(mayamClient).isTitleOrDescendentsProtected(PROTECTED_TITLE);

		Util.deleteFiles(pp);
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testDeletePackageProcessing() throws MessageProcessingFailedException, MayamClientException, MessageValidationException
	{
		
		PlaceholderMessage pm =buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		
		DeletePackage dp = (DeletePackage) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deletePackage(dp)).thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.packageExists(EXISTING_PACKAGE_ID)).thenReturn(true);
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		
		//the call we are testing
		try
		{
			processor.deletePackage((DeletePackage) pm.getActions()
			                                          .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
			                                          .get(0));
		}
		catch (MessageValidationException e)
		{
			fail();
		}

		//verify expected calls
		verify(mayamClient).deletePackage(dp);
		
		
	}
	
	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testDeletePackageProcessingFails() throws MessageProcessingFailedException, MessageValidationException, MayamClientException
	{
		
	PlaceholderMessage pm =buildDeletePackageRequest(false,EXISTING_TITLE,EXISTING_PACKAGE_ID);
	MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);	
	
		DeletePackage dp = (DeletePackage) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deletePackage(dp)).thenReturn(MayamClientErrorCode.PACKAGE_UPDATE_FAILED);
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		when(mayamClient.materialExists(EXISTING_MATERIAL_ID)).thenReturn(true);
		when(mayamClient.packageExists(EXISTING_PACKAGE_ID)).thenReturn(true);
		
		//the call we are testing
		processor.deletePackage((DeletePackage) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
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
	*/

}
