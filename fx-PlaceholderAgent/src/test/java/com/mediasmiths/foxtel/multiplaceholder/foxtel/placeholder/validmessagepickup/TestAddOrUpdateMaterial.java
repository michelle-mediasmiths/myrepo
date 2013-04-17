package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.validmessagepickup;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.messagecreation.elementgenerators.MSItem;
import com.mediasmiths.mayam.MayamClientErrorCode;
import org.apache.commons.lang.RandomStringUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestAddOrUpdateMaterial extends ValidMessagePickTest
{

	public TestAddOrUpdateMaterial() throws JAXBException, SAXException {
		super();
	}

	public PlaceholderMessage generatePlaceholderMessage() throws Exception {

		PlaceholderMessage message = new PlaceholderMessage();
		//message.setMessageID(RandomStringUtils.randomAlphabetic(6));
		message.setMessageID("TestAddOrUpdateMaterial_"+RandomStringUtils.randomAlphabetic(6));

		message.setSenderID(RandomStringUtils.randomAlphabetic(6));

		HelperMethods helper = new HelperMethods();
		String titleId = helper.generateTitleID();

		MSItem msItem = new MSItem();
		MaterialType material = new MaterialType();
		material = msItem.validItem(material, titleId);

		AddOrUpdateMaterial AddOrUpdateMaterial = new AddOrUpdateMaterial();
		AddOrUpdateMaterial.setTitleID(titleId);
		AddOrUpdateMaterial.setMaterial(material);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				AddOrUpdateMaterial);
		message.setActions(actions);

		return message;
	}

	@Override
	protected void mockValidCalls(PlaceholderMessage message) throws Exception {

		// make mayamclient say any title exists
		when(mayamClient.titleExists(anyString()))
				.thenReturn(new Boolean(true));
		// make maysamclient say no materials exists
		when(mayamClient.materialExists(anyString())).thenReturn(
				new Boolean(false));
		// return success status on mayamClient material create
		when(mayamClient.createMaterial((MaterialType) anyObject(),anyString()))
				.thenReturn(MayamClientErrorCode.SUCCESS);
	}

	@Override
	protected void verifyValidCalls(PlaceholderMessage message) {

		verify(mayamClient).createMaterial((MaterialType) anyObject(),anyString());

	}

	@Override
	protected void mockInValidCalls(PlaceholderMessage message)
			throws Exception {
		// make mayamclient say any title exists
		when(mayamClient.titleExists(anyString()))
				.thenReturn(new Boolean(true));
		// make maysamclient say no materials exists
		when(mayamClient.materialExists(anyString())).thenReturn(
				new Boolean(false));
		// return success status on mayamClient material create
		when(mayamClient.createMaterial((MaterialType) anyObject(),anyString()))
				.thenReturn(MayamClientErrorCode.MATERIAL_CREATION_FAILED);

	}

	@Override
	protected void verifyInValidCalls(PlaceholderMessage message)
			throws Exception {
		verify(mayamClient).createMaterial((MaterialType) anyObject(),anyString());

	}

	@Override
	protected String getFileName() {
		return "testAddOrUpdateMaterial.xml";
	}

}
