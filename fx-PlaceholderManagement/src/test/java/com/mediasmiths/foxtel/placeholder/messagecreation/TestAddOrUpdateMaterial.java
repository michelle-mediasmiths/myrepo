package com.mediasmiths.foxtel.placeholder.messagecreation;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.MSItem;

public class TestAddOrUpdateMaterial extends PlaceHolderMessageTest{

	public TestAddOrUpdateMaterial() throws JAXBException, SAXException {
		super();
	}

	protected PlaceholderMessage generatePlaceholderMessage()
			throws Exception {

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");

		HelperMethods method = new HelperMethods();
		String titleId = method.validTitleId();

		MSItem msItem = new MSItem();
		MaterialType material = new MaterialType();
		material = msItem.validItem(material, titleId);

		AddOrUpdateMaterial AddOrUpdateMaterial = new AddOrUpdateMaterial();
		AddOrUpdateMaterial.setTitleID(titleId);
		AddOrUpdateMaterial.setMaterial(material);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(AddOrUpdateMaterial);
		message.setActions(actions);

		return message;
	}

	@Override
	protected String getFileName() {
		return "testAddOrUpdateMaterial.xml";
	}
}
