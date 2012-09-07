package com.mediasmiths.foxtel.placeholder.messagecreation;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.generated.MediaExchange.Programme.Detail;
import com.mediasmiths.foxtel.generated.MediaExchange.Programme.Media;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidationResult;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public abstract class PlaceHolderMessageTest {

	protected abstract PlaceholderMessage generatePlaceholderMessage () throws Exception;
	protected abstract String getFileName();
	private PlaceHolderMessageValidator toTest;
	
	protected String getFilePath(){
		return "/tmp/Foxtel/TestData/" + getFileName();
	}
	
	public PlaceHolderMessageTest() throws JAXBException, SAXException{
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

			@Override
			public boolean titleExists(String titleID)
					throws MayamClientException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean materialExists(String materialID)
					throws MayamClientException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isMaterialForPackageProtected(String packageID) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isTitleOrDescendentsProtected(String titleID)
					throws MayamClientException {
				// TODO Auto-generated method stub
				return false;
			}
		});

	}
	
	@Test
	public final void testWritePlaceHolderMessage () throws Exception {
		
		PlaceholderMessage message = this.generatePlaceholderMessage();
		FileWriter writer = new FileWriter();
		writer.writeObjectToFile(message, getFilePath());
		//test that the generated placeholder message is valid
		assertEquals(toTest.validateFile(getFilePath()),PlaceHolderMessageValidationResult.IS_VALID);
		
	}
	
}
