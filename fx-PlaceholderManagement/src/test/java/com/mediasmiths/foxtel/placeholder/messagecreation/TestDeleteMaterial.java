package com.mediasmiths.foxtel.placeholder.messagecreation;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.RandomStringUtils;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.mayam.MayamClientErrorCode;

public class TestDeleteMaterial extends PlaceHolderMessageTest{

	public TestDeleteMaterial() throws JAXBException, SAXException {
		super();
	}

	protected PlaceholderMessage generatePlaceholderMessage() throws Exception {
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID(RandomStringUtils.randomAlphabetic(6));
		message.setSenderID(RandomStringUtils.randomAlphabetic(6));

		DeleteMaterial DeleteMaterial = new DeleteMaterial();
		DeleteMaterial.setTitleID("Gavin and Stacey");

		Material item = new Material();
		item.setMaterialID("fgh456");
		DeleteMaterial.setMaterial(item);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(DeleteMaterial);
		message.setActions(actions);

		return message;
	}
	
	@Override
	protected void mockCalls(PlaceholderMessage message) throws Exception {
		when(mayamClient.deleteMaterial((DeleteMaterial) anyObject())).thenReturn(MayamClientErrorCode.SUCCESS);
	}

	@Override
	protected void verifyCalls(PlaceholderMessage message) {
		verify(mayamClient).deleteMaterial((DeleteMaterial) anyObject());
	}

	@Override
	protected String getFileName() {
		return "testDeleteMaterial.xml";
	}

	
}
