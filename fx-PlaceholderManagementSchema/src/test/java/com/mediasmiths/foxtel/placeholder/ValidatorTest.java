package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;


public class ValidatorTest {
	
	private static Logger logger = Logger.getLogger(ValidatorTest.class);

	private String[] shouldValidate = { "test1_createTitle.xml" };
	private String[] shouldNotValidate = { "test10_deleteTitleFAIL.xml" };

	@Test
	/**
	 * Applies the validator to pregenerated xml files under src/test/resources
	 */
	public void testValidateFiles() throws IOException, SAXException,
			ParserConfigurationException {

		logger.info("About to try and validate files we expect to be valid");
		for (String filename : shouldValidate) {
			logger.info(filename);
			String testFilePath = copyResourceToTempFile("testFiles/"
					+ filename);
			logger.trace(String.format("File contents %s", FileUtils.readFileToString(new File(testFilePath))));
			
			assertTrue(Validator.validateFile(testFilePath));
		}

		logger.info("About to try and validate files we expect to be invalid");
		for (String filename : shouldNotValidate) {
			logger.info(filename);
			String testFilePath = copyResourceToTempFile("testFiles/"
					+ filename);
			logger.trace(String.format("File contents %s", FileUtils.readFileToString(new File(testFilePath))));
			assertFalse(Validator.validateFile(testFilePath));

		}

	}

	/**
	 * Copies the given resource to a temporary file and returns the path to the
	 * new file
	 * 
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private String copyResourceToTempFile(String resourcePath)
			throws FileNotFoundException, IOException {

		// file in temp directory we are copying the requested resource to
		File outputFile = new File(FileUtils.getTempDirectoryPath()
				+ System.getProperty("file.separator") + resourcePath);
		InputStream resourceAsStream = getClass().getClassLoader()
				.getResourceAsStream(resourcePath);

		// create any require directories
		new File(outputFile.getAbsolutePath()).mkdirs();
		//delete file if it already exists
		outputFile.delete();

		IOUtils.copy(resourceAsStream, new FileOutputStream(outputFile));
		return outputFile.getAbsolutePath();
	}

}
