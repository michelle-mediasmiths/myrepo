package com.mediasmiths.foxtel.placeholder.messagecreation;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

public class TestDeleteMaterial extends PlaceHolderMessageTest{

	public TestDeleteMaterial() throws JAXBException, SAXException {
		super();
	}

	protected PlaceholderMessage generatePlaceholderMessage() throws Exception {
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");

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
	protected String getFileName() {
		return "testDeleteMaterial.xml";
	}
}
