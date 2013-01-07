package com.mediasmiths.foxtel.placeholder.validmessagepickup;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.RandomStringUtils;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.Package;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class TestDeletePackage extends ValidMessagePickTest {

	protected String PACKAGE_ID="abc123";
	
	public TestDeletePackage() throws JAXBException, SAXException {
		super();
	}

	protected PlaceholderMessage generatePlaceholderMessage() throws Exception {

		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID(RandomStringUtils.randomAlphabetic(6));
		message.setSenderID(RandomStringUtils.randomAlphabetic(6));

		DeletePackage deleteTx = generateDeletePackage();

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				deleteTx);
		message.setActions(actions);

		return message;
	}

	private DeletePackage generateDeletePackage() {

		DeletePackage deleteTx = new DeletePackage();
		HelperMethods method = new HelperMethods();
		String titleId = method.generateTitleID();
		Package txPackage = new Package();
		txPackage.setPresentationID(PACKAGE_ID);
		deleteTx.setPackage(txPackage);
		deleteTx.setTitleID(titleId);

		return deleteTx;
	}

	@Override
	protected void mockValidCalls(PlaceholderMessage message) throws Exception {
		when(mayamClient.packageExistsForMaterial((String) anyObject(),(String) anyObject())).thenReturn(true);
		when(mayamClient.isMaterialForPackageProtected((String) anyObject(), (String) anyObject())).thenReturn(new Boolean(false));
		when(mayamClient.deletePackage((DeletePackage) anyObject()))
				.thenReturn(MayamClientErrorCode.SUCCESS); 
	}

	@Override
	protected void verifyValidCalls(PlaceholderMessage message) {
		verify(mayamClient).deletePackage((DeletePackage) anyObject());
	}

	@Override
	protected String getFileName() {
		return "testDeletePackage.xml";
	}

	@Override
	protected void mockInValidCalls(PlaceholderMessage mesage) throws Exception {
		when(mayamClient.packageExistsForMaterial((String) anyObject(),(String) anyObject())).thenReturn(true);
		when(mayamClient.isMaterialForPackageProtected((String) anyObject(),(String) anyObject())).thenReturn(new Boolean(false));
		when(mayamClient.deletePackage((DeletePackage) anyObject()))
				.thenReturn(MayamClientErrorCode.PACKAGE_UPDATE_FAILED);

	}

	@Override
	protected void verifyInValidCalls(PlaceholderMessage message)
			throws Exception {
		verifyValidCalls(message);

	}

}
