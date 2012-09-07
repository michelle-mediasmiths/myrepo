package com.mediasmiths.foxtel.placeholder.requestValidation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.generated.MediaExchange.Programme.Detail;
import com.mediasmiths.foxtel.generated.MediaExchange.Programme.Media;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;

public abstract class PlaceHolderMessageValidatorTest {

	private PlaceHolderMessageValidator toTest;

	public PlaceHolderMessageValidatorTest() throws JAXBException, SAXException {

		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		toTest = new PlaceHolderMessageValidator(unmarhsaller, new MayamClient() {
			
			@Override
			public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode updateTitle(Detail programme) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode updatePackage() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode updatePackage(PackageType txPackage) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode updateMaterial(MaterialType material) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode updateMaterial(Media media) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void shutdown() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public MayamClientErrorCode purgeTitle(PurgeTitle title) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode purgePackage() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode createTitle(CreateOrUpdateTitle title) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode createTitle(Detail title) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode createPackage() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode createPackage(PackageType txPackage) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode createMaterial(MaterialType material) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MayamClientErrorCode createMaterial(Media media) {
				// TODO Auto-generated method stub
				return null;
			}
		});

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
