package com.mediasmiths.foxtel.mpa.validation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.mpa.ProgrammeMaterialTest;
import com.mediasmiths.foxtel.mpa.ResultLogger;

public class ProgrammeMaterialValidationTest_FXT_4_6_0 extends ValidationTest {
	
	private static Logger logger = Logger.getLogger(ProgrammeMaterialValidationTest_FXT_4_6_0.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	public ProgrammeMaterialValidationTest_FXT_4_6_0() throws JAXBException, IOException,
			SAXException {
		super();
	}

	
	@Test
	public void testMaterialNoPackages() throws Exception{
		
		Material material = ProgrammeMaterialTest.getMaterialNoPackages(EXISTING_TITLE, EXISTING_MATERIAL_IS_PLACEHOLDER);
		assertEquals(MessageValidationResult.IS_VALID,validationForMaterial(material));
		
		verify(mayamClient).titleExists(EXISTING_TITLE);
		verify(mayamClient).materialExists(EXISTING_MATERIAL_IS_PLACEHOLDER);
		verify(mayamClient).materialHasPassedPreview(EXISTING_MATERIAL_IS_PLACEHOLDER);
		verify(mayamClient).isMaterialPlaceholder(EXISTING_MATERIAL_IS_PLACEHOLDER);
	}
	
	@Test
	public void testTitleDoesntExist_FXT_4_6_0_2() throws Exception{
		logger.info( "Starting FXT 4.6.0.2  - Programme material message references unknown title");
		
		Material material = ProgrammeMaterialTest.getMaterialNoPackages(NOT_EXISTING_TITLE, EXISTING_MATERIAL_IS_PLACEHOLDER);

		MessageValidationResult validateFile = validationForMaterial(material);
		if (MessageValidationResult.TITLE_DOES_NOT_EXIST==validateFile)
		{
			resultLogger.info("FXT 4.6.0.2  - Programme material message references unknown title --Passed");
		}
		else
		{
			resultLogger.info("FXT 4.6.0.2  - Programme material message references unknown title --Failed");
		}
		assertEquals(MessageValidationResult.TITLE_DOES_NOT_EXIST,validateFile);
		
		verify(mayamClient).titleExists(NOT_EXISTING_TITLE);
	}
	
	@Test
	public void testTitleCheckFails() throws Exception {
		
		Material material = ProgrammeMaterialTest.getMaterialNoPackages(EXISTING_TITLE_CHECK_FAILS, EXISTING_MATERIAL_IS_PLACEHOLDER);
		assertEquals(MessageValidationResult.MAYAM_CLIENT_ERROR,validationForMaterial(material));
		
		verify(mayamClient).titleExists(EXISTING_TITLE_CHECK_FAILS);
		
	}
	
	@Test
	public void testMaterialIsNotPlaceholder_FXT_4_6_0_5() throws Exception{
		
		logger.info("Starting FXT 4.6.0.5  - Programme material message references non placeholder item");

		Material material = ProgrammeMaterialTest.getMaterialNoPackages(EXISTING_TITLE, EXISTING_MATERIAL_NOT_PLACEHOLDER);
				
		MessageValidationResult validateFile = validationForMaterial(material);
		if (MessageValidationResult.MATERIAL_IS_NOT_PLACEHOLDER==validateFile)
		{
			resultLogger.info("FXT 4.6.0.5  - Programme material message references non placeholder item --Passed");
		}
		else
		{
			resultLogger.info("FXT 4.6.0.5  - Programme material message references non placeholder item --Failed");
		}
		assertEquals(MessageValidationResult.MATERIAL_IS_NOT_PLACEHOLDER,validateFile);
				
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
		
	}
	
	@Test
	public void testMaterialDoesntExist_FXT_4_6_0_3() throws Exception {
		logger.info("Starting FXT 4.6.0.3  - Programme material message references unknown material");
		
		Material material = ProgrammeMaterialTest.getMaterialNoPackages(EXISTING_TITLE, NOT_EXISTING_MATERIAL);
		
		MessageValidationResult validateFile = validationForMaterial(material);
		if (MessageValidationResult.MATERIAL_DOES_NOT_EXIST==validateFile)
		{
			resultLogger.info("FXT 4.6.0.3  - Programme material message references unknown material --Passed");
		}
		else
		{
			resultLogger.info("FXT 4.6.0.3  - Programme material message references unknown material --Failed");
		}
		assertEquals(MessageValidationResult.MATERIAL_DOES_NOT_EXIST,validateFile);
				
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
