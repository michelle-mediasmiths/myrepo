package com.mediasmiths.foxtel.placeholder.requestValidation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidationResult;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class AddOrUpdateMaterialTest extends PlaceHolderMessageValidatorTest {

	public AddOrUpdateMaterialTest() throws JAXBException, SAXException {
		super();
	}

	@Test
	public void testValidAddMaterial() throws Exception {

		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "validAddMaterial");

		// prepare mock mayamClient
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);

		// test that the generated placeholder message is valid
		assertEquals(PlaceHolderMessageValidationResult.IS_VALID,
				toTest.validateFile(temp.getAbsolutePath()));
	}

	@Test
	public void testAddMaterialTitleDoesntExist() throws Exception {
		PlaceholderMessage pm = buildAddMaterialRequest(NOT_EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "addMaterialTitleDoesntExist");

		// prepare mock mayamClient
		when(mayamClient.titleExists(NOT_EXISTING_TITLE)).thenReturn(false);

		// test that the generated placeholder message fails validation for the
		// correct reason
		assertEquals(
				PlaceHolderMessageValidationResult.NO_EXISTING_TITLE_FOR_MATERIAL,
				toTest.validateFile(temp.getAbsolutePath()));
	}

	@Test
	public void testAddMaterialOrderCreatedAfterRequiredBy()
			throws IOException, Exception {
		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE,
				JAN10th, JAN1st);
		File temp = createTempXMLFile(pm, "addMaterialTitleDoesntExist");
		// prepare mock mayamClient
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		// test that the generated placeholder message fails validation for the
		// correct reason
		assertEquals(
				PlaceHolderMessageValidationResult.ORDER_CREATED_AND_REQUIREDBY_DATES_NOT_IN_ORDER,
				toTest.validateFile(temp.getAbsolutePath()));
	}

	@Test(expected = MayamClientException.class)
	public void testAddMaterialTitleExistRequestFails() throws Exception {
		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);
		File temp = createTempXMLFile(pm, "addMaterialTitleExistRequestFails");

		// prepare mock mayamClient
		when(mayamClient.titleExists(EXISTING_TITLE)).thenThrow(
				new MayamClientException(MayamClientErrorCode.FAILURE));

		// try to call validation, expect an exception
		toTest.validateFile(temp.getAbsolutePath());
	}

}
