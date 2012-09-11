package com.mediasmiths.foxtel.placeholder.messagecreation;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.MSTxPackage;
import com.mediasmiths.mayam.MayamClientException;

public class TestAddOrUpdatePackage extends PlaceHolderMessageTest{

	public TestAddOrUpdatePackage() throws JAXBException, SAXException {
		super();
	}

	protected PlaceholderMessage generatePlaceholderMessage()
			throws DatatypeConfigurationException {

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("123abc");
		message.setSenderID("987xyz");

		AddOrUpdatePackage addTxPackage = generateAddOrUpdatePackage();

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(addTxPackage);
		message.setActions(actions);

		return message;
	}
	
	@Override
	protected void mockCalls(){
		
		//make mayamclient say any material exists
		
		try {
			when(mayamClient.materialExists(anyString())).thenReturn(new Boolean(true));
		} catch (MayamClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private AddOrUpdatePackage generateAddOrUpdatePackage()
			throws DatatypeConfigurationException {

		AddOrUpdatePackage addTxPackage = new AddOrUpdatePackage();

		HelperMethods method = new HelperMethods();
		String titleId = method.validTitleId();
		MSTxPackage msTxPackage = new MSTxPackage();
		PackageType txPackage = new PackageType();
		txPackage = msTxPackage.validTxPackage(txPackage, titleId);

		addTxPackage.setTitleID(titleId);
		addTxPackage.setPackage(txPackage);

		return addTxPackage;
	}

	@Override
	protected String getFileName() {
		return "testAddOrUpdatePackage.xml";
	}

}
