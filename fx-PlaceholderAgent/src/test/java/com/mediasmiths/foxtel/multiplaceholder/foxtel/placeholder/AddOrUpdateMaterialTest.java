package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder;

import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddOrUpdateMaterialTest extends PlaceHolderMessageShortTest
{

	public AddOrUpdateMaterialTest() throws JAXBException, SAXException, IOException {
		super();
	}

	@Test
	@Category(ValidationTests.class)
	public void testValidAddMaterialValidation() throws Exception {

		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (pm, "validAddMaterial");

		// prepare mock mayamClient
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		try
		{
			validator.validateAddOrUpdateMaterial((AddOrUpdateMaterial) pm.getActions()
			                                                              .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
			                                                              .get(0));
		}
		catch (MessageValidationException r)
		{
			fail();
		}

		Util.deleteFiles(pp);
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testValidAddMaterialProcessing() throws Exception {

		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		
		AddOrUpdateMaterial aoum = (AddOrUpdateMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		// prepare mock mayamClient
		when(mayamClient.materialExists(NEW_MATERIAL_ID)).thenReturn(false);
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		when(mayamClient.createMaterial(aoum.getMaterial(),EXISTING_TITLE)).thenReturn(MayamClientErrorCode.SUCCESS);
		//the call we are testing
		processor.processMessage(envelope);
		//check create material was called
		verify(mayamClient).createMaterial(aoum.getMaterial(),EXISTING_TITLE);
		
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testValidUpdateMaterialProcessing() throws Exception {

		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		
		AddOrUpdateMaterial aoum = (AddOrUpdateMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		// prepare mock mayamClient
		when(mayamClient.materialExists(NEW_MATERIAL_ID)).thenReturn(true);
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		when(mayamClient.updateMaterial(aoum.getMaterial())).thenReturn(MayamClientErrorCode.SUCCESS);
		//the call we are testing
		processor.processMessage(envelope);
		//check create material was called
		verify(mayamClient).updateMaterial(aoum.getMaterial());
		
	}

	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testValidAddMaterialProcessingFailsOnQueryingExistingMaterial() throws Exception {

		//test that we get a MessageProcessingFailedException when the query on existing material failes
		
		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		// prepare mock mayamClient
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		when(mayamClient.materialExists(NEW_MATERIAL_ID)).thenThrow(new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION));
		//the call we are testing
		processor.addOrUpdateMaterial((AddOrUpdateMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
	}
	
	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testValidAddMaterialProcessingFailesOnCreateMaterial() throws Exception {

		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new PickupPackage("xml"), pm);
		AddOrUpdateMaterial aoum = (AddOrUpdateMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		// prepare mock mayamClient
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		when(mayamClient.materialExists(NEW_MATERIAL_ID)).thenReturn(false);
		when(mayamClient.createMaterial(aoum.getMaterial(),EXISTING_TITLE)).thenReturn(MayamClientErrorCode.MATERIAL_CREATION_FAILED);
		//the call we are testing
		processor.addOrUpdateMaterial((AddOrUpdateMaterial) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
		
	}
	

	@Test
	@Category(ValidationTests.class)
	public void testAddMaterialTitleDoesntExist() throws Exception {
		PlaceholderMessage pm = buildAddMaterialRequest(NOT_EXISTING_TITLE);
		PickupPackage pp = createTempXMLFile (pm, "addMaterialTitleDoesntExist");

		// prepare mock mayamClient
		when(mayamClient.titleExists(NOT_EXISTING_TITLE)).thenReturn(false);

		// test that the generated placeholder message fails validation for the
		// correct reason

		try
		{
				validator.validateAddOrUpdateMaterial((AddOrUpdateMaterial)pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
			    fail();
		}
		catch (MessageValidationException r)
		{
			assertEquals(
					    MessageValidationResult.NO_EXISTING_TITLE_FOR_MATERIAL, r.result);
		}
		Util.deleteFiles(pp);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testAddMaterialMaterialIDEmpty() throws Exception {
		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);
		
		((AddOrUpdateMaterial)pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0)).getMaterial().setMaterialID("");
		PickupPackage pp = createTempXMLFile (pm, "addMaterialTitleMaterialIDEmpty");

		// prepare mock mayamClient
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);


		try
		{
			validator.validateAddOrUpdateMaterial((AddOrUpdateMaterial)pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
			fail();
		}
		catch (MessageValidationException r)
		{
			assertEquals(MessageValidationResult.MATERIALID_IS_NULL_OR_EMPTY, r.result);
		}
		Util.deleteFiles(pp);
	}

	@Test
	@Category(ValidationTests.class)
	public void testAddMaterialOrderCreatedAfterRequiredBy()
			throws IOException, Exception {
		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE,
				JAN10th, JAN1st);
		PickupPackage pp = createTempXMLFile (pm, "addMaterialTitleDoesntExist");
		// prepare mock mayamClient
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		//dont fail messages for dates out of order


		try
		{
			validator.validateAddOrUpdateMaterial((AddOrUpdateMaterial) pm.getActions()
			                                                              .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
			                                                              .get(0));
		}
		catch (MessageValidationException r)
		{
			fail();
		}

		Util.deleteFiles(pp);
	}

	@Test
	@Category(ValidationTests.class)
	public void testAddMaterialTitleExistRequestFails() throws Exception {
		PlaceholderMessage pm = buildAddMaterialRequest(ERROR_TITLE_ID);
		PickupPackage pp = createTempXMLFile (pm, "addMaterialTitleExistRequestFails");

		// prepare mock mayamClient
		when(mayamClient.titleExists(ERROR_TITLE_ID)).thenThrow(
				new MayamClientException(MayamClientErrorCode.FAILURE));


		try
		{
			validator.validateAddOrUpdateMaterial((AddOrUpdateMaterial)pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));
			fail();
		}
		catch (MessageValidationException r)
		{
			assertEquals(MessageValidationResult.MAYAM_CLIENT_ERROR, r.result);
		}
		// try to call validation, expect a mayam client error

		Util.deleteFiles(pp);
	}

}
