package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.messagetests.ResultLogger;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteMaterialTest_FXT_4_1_27_28_29 extends PlaceHolderMessageShortTest
{
	private static Logger logger = Logger.getLogger(DeleteMaterialTest_FXT_4_1_27_28_29.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	public DeleteMaterialTest_FXT_4_1_27_28_29() throws JAXBException, SAXException, IOException {
		super();
	}

	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialNotProtected() throws IOException, Exception {
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (pm, "validDeleteMaterialTitleNotProtected");
		
		when(mayamClient.materialExists(EXISTING_MATERIAL_ID)).thenReturn(true);
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenReturn(false);
		
		validator.validateDeleteMaterial((DeleteMaterial) pm.getActions()
		                                                    .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
		                                                    .get(0));
		
		verify(mayamClient).isTitleOrDescendentsProtected(EXISTING_TITLE);
		Util.deleteFiles(pp);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialIsProtected_FXT_4_1_27_28_29() throws IOException, Exception {
		logger.info("Starting FXT 4.1.27/28/29 ");

		PlaceholderMessage pm = buildDeleteMaterialRequest(true,PROTECTED_TITLE);
		PickupPackage pp = createTempXMLFile (pm, "validDeleteMaterialTitleIsProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(PROTECTED_TITLE)).thenReturn(true);
		

		try
		{
			validator.validateDeleteMaterial((DeleteMaterial) pm.getActions()
			                                                    .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
			                                                    .get(0));
			resultLogger.info("FXT 4.1.27/28/29  --Failed");
			fail();

		}
		catch (MessageValidationException r)
		{
			assertEquals(MessageValidationResult.TITLE_OR_DESCENDANT_IS_PROTECTED, r.result);
			resultLogger.info("FXT 4.1.27/28/29 --Passed");

		}

		verify(mayamClient).isTitleOrDescendentsProtected(PROTECTED_TITLE);
		Util.deleteFiles(pp);
	}
	

	@Test
	@Category(ValidationTests.class)
	public void testDeleteMaterialProtectedCheckFails() throws IOException, Exception {
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (pm, "validDeleteMaterialTitleNotProtected");
		
		when(mayamClient.isTitleOrDescendentsProtected(EXISTING_TITLE)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));

		try
		{
			validator.validateDeleteMaterial((DeleteMaterial)pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
		}
		catch (MessageValidationException r)
		{
			assertEquals(MessageValidationResult.MAYAM_CLIENT_ERROR,r.result);
		}
		
		verify(mayamClient).isTitleOrDescendentsProtected(EXISTING_TITLE);
		Util.deleteFiles(pp);
	}
	

	@Test
	@Category(ProcessingTests.class)
	public void testDeleteMaterialProcessing() throws DatatypeConfigurationException, MessageProcessingFailedException, MessageValidationException, MayamClientException
	{
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		
		DeleteMaterial dm = (DeleteMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deleteMaterial(dm)).thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.materialExists(EXISTING_MATERIAL_ID)).thenReturn(true);
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);



		//the call we are testing
		processor.deleteMaterial((DeleteMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
		
		//verify expected calls
		verify(mayamClient).deleteMaterial(dm);
		
		
	}
	
	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testDeleteMaterialProcessingFails() throws DatatypeConfigurationException, MessageProcessingFailedException, MessageValidationException, MayamClientException
	{
		
		PlaceholderMessage pm = buildDeleteMaterialRequest(false,EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		
		DeleteMaterial dm = (DeleteMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		//prepare mock mayam client
		when(mayamClient.deleteMaterial(dm)).thenReturn(MayamClientErrorCode.MATERIAL_UPDATE_FAILED);
		when(mayamClient.materialExists(EXISTING_MATERIAL_ID)).thenReturn(true);
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);



		//the call we are testing
		processor.deleteMaterial((DeleteMaterial) pm.getActions()
		                                            .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
		                                            .get(0));
	}

	private PlaceholderMessage buildDeleteMaterialRequest(boolean materialProtected, String titleID) throws DatatypeConfigurationException {
		
		Material m = new Material();
		m.setMaterialID(EXISTING_MATERIAL_ID);
		
		DeleteMaterial dm = new DeleteMaterial();
		dm.setTitleID(titleID);
		dm.setMaterial(m);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				dm);
		
		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(createMessageID());
		pm.setSenderID(createSenderID());
		pm.setActions(actions);
		return pm;
		
	}

	
}
