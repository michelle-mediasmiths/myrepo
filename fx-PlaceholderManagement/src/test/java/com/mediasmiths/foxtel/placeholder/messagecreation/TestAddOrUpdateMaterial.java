package com.mediasmiths.foxtel.placeholder.messagecreation;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.RandomStringUtils;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.MSItem;
import com.mediasmiths.mayam.MayamClientException;

public class TestAddOrUpdateMaterial extends PlaceHolderMessageTest{

	public TestAddOrUpdateMaterial() throws JAXBException, SAXException {
		super();
	}

	protected PlaceholderMessage generatePlaceholderMessage()
			throws Exception {

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID(RandomStringUtils.random(6));
		message.setSenderID(RandomStringUtils.random(6));

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
	protected void mockCalls(){
		
		//make mayamclient say any title exists
		
		try {
			when(mayamClient.titleExists(anyString())).thenReturn(new Boolean(true));
		} catch (MayamClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getFileName() {
		return "testAddOrUpdateMaterial.xml";
	}
}
