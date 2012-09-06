package com.mediasmiths.foxtel.placeholder.requestValidation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidator;

public abstract class PlaceHolderMessageValidatorTest {

	private PlaceHolderMessageValidator toTest;

	public PlaceHolderMessageValidatorTest() throws JAXBException, SAXException {

		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		toTest = new PlaceHolderMessageValidator(unmarhsaller);

	}
	
	
	

//	@Test
//	/**
//	 * Applies the validator to pregenerated xml files under src/test/resources
//	 */
//	public void testValidateFiles() throws IOException, SAXException,
//			ParserConfigurationException {
//
//		logger.info("About to try and validate files we expect to be valid");
//		for (String filename : shouldValidate) {
//			logger.info(filename);
//			String testFilePath = copyResourceToTempFile("testFiles/"
//					+ filename);
//			logger.trace(String.format("File contents %s",
//					FileUtils.readFileToString(new File(testFilePath))));
//
//			assertEquals(PlaceHolderMessageValidationResult.IS_VALID,
//					toTest.validateFile(testFilePath));
//		}
//
//		logger.info("About to try and validate files we expect to be invalid");
//		for (String filename : shouldNotValidate) {
//			logger.info(filename);
//			String testFilePath = copyResourceToTempFile("testFiles/"
//					+ filename);
//			logger.trace(String.format("File contents %s",
//					FileUtils.readFileToString(new File(testFilePath))));
//			assertTrue(toTest.validateFile(testFilePath) != PlaceHolderMessageValidationResult.IS_VALID);
//
//		}
//
//	}
//
//	/**
//	 * Copies the given resource to a temporary file and returns the path to the
//	 * new file
//	 * 
//	 * @return
//	 * @throws IOException
//	 * @throws FileNotFoundException
//	 */
//	private String copyResourceToTempFile(String resourcePath)
//			throws FileNotFoundException, IOException {
//
//		// file in temp directory we are copying the requested resource to
//		File outputFile = new File(FileUtils.getTempDirectoryPath()
//				+ System.getProperty("file.separator") + resourcePath);
//		InputStream resourceAsStream = getClass().getClassLoader()
//				.getResourceAsStream(resourcePath);
//
//		// create any require directories
//		new File(outputFile.getAbsolutePath()).mkdirs();
//		// delete file if it already exists
//		outputFile.delete();
//
//		IOUtils.copy(resourceAsStream, new FileOutputStream(outputFile));
//		return outputFile.getAbsolutePath();
//	}

}
