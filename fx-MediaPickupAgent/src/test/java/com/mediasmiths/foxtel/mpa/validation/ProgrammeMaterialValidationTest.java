package com.mediasmiths.foxtel.mpa.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.mpa.ProgrammeMaterialTest;

public class ProgrammeMaterialValidationTest extends ValidationTest {

	public ProgrammeMaterialValidationTest() throws JAXBException, IOException,
			SAXException {
		super();
	}

	
	@Test
	public void testMaterialNoPackages() throws Exception{
		
		Material material = ProgrammeMaterialTest.getMaterialNoPackages(EXISTING_TITLE, EXISTING_MATERIAL_IS_PLACEHOLDER);
		assertEquals(MessageValidationResult.IS_VALID,validationForMaterial(material));
		
		verify(mayamClient).titleExists(EXISTING_TITLE);
		verify(mayamClient).materialExists(EXISTING_MATERIAL_IS_PLACEHOLDER);
		verify(mayamClient).isMaterialPlaceholder(EXISTING_MATERIAL_IS_PLACEHOLDER);
	}
	
	@Test
	public void testTitleDoesntExist() throws Exception{
		Material material = ProgrammeMaterialTest.getMaterialNoPackages(NOT_EXISTING_TITLE, EXISTING_MATERIAL_IS_PLACEHOLDER);
		assertEquals(MessageValidationResult.TITLE_DOES_NOT_EXIST,validationForMaterial(material));
		
		verify(mayamClient).titleExists(NOT_EXISTING_TITLE);
	}
	
	@Test
	public void testTitleCheckFails() throws Exception {
		
		Material material = ProgrammeMaterialTest.getMaterialNoPackages(EXISTING_TITLE_CHECK_FAILS, EXISTING_MATERIAL_IS_PLACEHOLDER);
		assertEquals(MessageValidationResult.MAYAM_CLIENT_ERROR,validationForMaterial(material));
		
		verify(mayamClient).titleExists(EXISTING_TITLE_CHECK_FAILS);
		
	}
	
	@Test
	public void testMaterialIsNotPlaceholder() throws Exception{
		Material material = ProgrammeMaterialTest.getMaterialNoPackages(EXISTING_TITLE, EXISTING_MATERIAL_NOT_PLACEHOLDER);
		assertEquals(MessageValidationResult.MATERIAL_IS_NOT_PLACEHOLDER,validationForMaterial(material));
		
		verify(mayamClient).titleExists(EXISTING_TITLE);
		verify(mayamClient).materialExists(EXISTING_MATERIAL_NOT_PLACEHOLDER);
		verify(mayamClient).isMaterialPlaceholder(EXISTING_MATERIAL_NOT_PLACEHOLDER);
	}
	
	@Test
	public void testMaterialWithPackages() throws Exception{
		
		List<String> packageids = new ArrayList<String>();
		packageids.add(EXISTING_PACKAGE);
		packageids.add(EXISTING_PACKAGE2);
		
		Material material = ProgrammeMaterialTest.getMaterialWithPackages(EXISTING_TITLE, EXISTING_MATERIAL_IS_PLACEHOLDER, packageids);
		assertEquals(MessageValidationResult.IS_VALID,validationForMaterial(material));
		
		
		verify(mayamClient).titleExists(EXISTING_TITLE);
		verify(mayamClient).materialExists(EXISTING_MATERIAL_IS_PLACEHOLDER);
		verify(mayamClient).isMaterialPlaceholder(EXISTING_MATERIAL_IS_PLACEHOLDER);
		verify(mayamClient).packageExists(EXISTING_PACKAGE);
		verify(mayamClient).packageExists(EXISTING_PACKAGE2);
		
	}
	
	@Test
	public void testPackageDoesntExist() throws Exception{
		List<String> packageids = new ArrayList<String>();
		packageids.add(EXISTING_PACKAGE);
		packageids.add(NOT_EXISTING_PACKAGE);
		
		Material material = ProgrammeMaterialTest.getMaterialWithPackages(EXISTING_TITLE, EXISTING_MATERIAL_IS_PLACEHOLDER, packageids);
		assertEquals(MessageValidationResult.PACKAGE_DOES_NOT_EXIST,validationForMaterial(material));
		
		
		verify(mayamClient).titleExists(EXISTING_TITLE);
		verify(mayamClient).materialExists(EXISTING_MATERIAL_IS_PLACEHOLDER);
		verify(mayamClient).isMaterialPlaceholder(EXISTING_MATERIAL_IS_PLACEHOLDER);
		verify(mayamClient).packageExists(EXISTING_PACKAGE);
		verify(mayamClient).packageExists(NOT_EXISTING_PACKAGE);
	}
	
	@Test
	public void testPackageCheckFails() throws Exception {
		List<String> packageids = new ArrayList<String>();
		packageids.add(EXISTING_PACKAGE);
		packageids.add(EXISTING_PACKAGE_CHECK_FAILS);
		
		Material material = ProgrammeMaterialTest.getMaterialWithPackages(EXISTING_TITLE, EXISTING_MATERIAL_IS_PLACEHOLDER, packageids);
		assertEquals(MessageValidationResult.MAYAM_CLIENT_ERROR,validationForMaterial(material));
		
		
		verify(mayamClient).titleExists(EXISTING_TITLE);
		verify(mayamClient).materialExists(EXISTING_MATERIAL_IS_PLACEHOLDER);
		verify(mayamClient).isMaterialPlaceholder(EXISTING_MATERIAL_IS_PLACEHOLDER);
		verify(mayamClient).packageExists(EXISTING_PACKAGE);
		verify(mayamClient).packageExists(EXISTING_PACKAGE_CHECK_FAILS);
	}

	@Test
	public void testMaterialDoesntExist() throws Exception {
		
		Material material = ProgrammeMaterialTest.getMaterialNoPackages(EXISTING_TITLE, NOT_EXISTING_MATERIAL);
		assertEquals(MessageValidationResult.MATERIAL_DOES_NOT_EXIST,validationForMaterial(material));
		
		verify(mayamClient).titleExists(EXISTING_TITLE);
		verify(mayamClient).materialExists(NOT_EXISTING_MATERIAL);
	}
	
	@Test
	public void testMaterialCheckFails() throws Exception {
		
		Material material = ProgrammeMaterialTest.getMaterialNoPackages(EXISTING_TITLE, EXISTING_MATERIAL_CHECK_FAILS);
		assertEquals(MessageValidationResult.MAYAM_CLIENT_ERROR,validationForMaterial(material));
		
		verify(mayamClient).titleExists(EXISTING_TITLE);
		verify(mayamClient).materialExists(EXISTING_MATERIAL_CHECK_FAILS);
	}
	
}
