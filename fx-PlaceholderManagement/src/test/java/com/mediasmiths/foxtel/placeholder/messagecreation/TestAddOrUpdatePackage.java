package com.mediasmiths.foxtel.placeholder.messagecreation;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
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
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class TestAddOrUpdatePackage extends PlaceHolderMessageTest {

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
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				addTxPackage);
		message.setActions(actions);

		return message;
	}

	@Override
	protected void mockCalls(PlaceholderMessage message) throws Exception {

		AddOrUpdatePackage addTxPackage = (AddOrUpdatePackage) message
				.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);

		// make mayamclient say the material exists
		when(
				mayamClient.materialExists(addTxPackage.getPackage()
						.getMaterialID())).thenReturn(new Boolean(true));
		// make mayamclient say the package does not exist
		when(
				mayamClient.packageExists(addTxPackage.getPackage()
						.getPresentationID())).thenReturn(new Boolean(false));
		// return success on createpackge
		when(mayamClient.createPackage((PackageType) anyObject())).thenReturn(
				MayamClientErrorCode.SUCCESS);

	}

	@Override
	protected void verifyCalls(PlaceholderMessage message) {

		AddOrUpdatePackage addTxPackage = (AddOrUpdatePackage) message
				.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);

		// make mayamclient say the material exists
		try {
			verify(mayamClient).materialExists(
					addTxPackage.getPackage().getMaterialID());
			// make mayamclient say the package does not exist
			verify(mayamClient).packageExists(
					addTxPackage.getPackage().getPresentationID());
			// return success on createpackge
			verify(mayamClient).createPackage((PackageType) anyObject());
		} catch (MayamClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
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
