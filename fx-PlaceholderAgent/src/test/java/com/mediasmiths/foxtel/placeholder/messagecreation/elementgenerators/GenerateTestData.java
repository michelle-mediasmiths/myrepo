package com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.validmessagepickup.FileWriter;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.TestAddOrUpdateMaterial;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.TestAddOrUpdatePackage;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.TestCreateOrUpdateTitle;

public class GenerateTestData {
	
	private static Logger logger = Logger.getLogger(GenerateTestData.class);

	private final int CREATE_TITLES_TO_GENERATE = 20;
	private final int CREATE_MATERIALS_TO_GENERATE = 20;
	private final int CREATE_PACKAGES_TO_GENERATE = 20;
	
	private final String DESTINATION="/tmp/placeHolderTestData";
	
	@Test
	public void generateTestData() throws IOException, Exception{
		
		for(int i=1;i< CREATE_TITLES_TO_GENERATE;i++){
			generateTitle();
		}
		
		for(int i=1;i< CREATE_MATERIALS_TO_GENERATE;i++){
			generateMaterial();
		}
		
		for(int i=1;i< CREATE_PACKAGES_TO_GENERATE;i++){
			generatePackage();
		}
		
		
	}

	private void generatePackage() throws IOException, Exception {
		
		PlaceholderMessage pm = new TestAddOrUpdatePackage().generatePlaceholderMessage();
		writePlaceHolderMessage(pm);
				
	}

	private void generateMaterial() throws JAXBException, SAXException, Exception {
		
		PlaceholderMessage pm = new TestAddOrUpdateMaterial().generatePlaceholderMessage();
		writePlaceHolderMessage(pm);
	}

	private void generateTitle() throws IOException, Exception {
		
		PlaceholderMessage pm = new TestCreateOrUpdateTitle().generatePlaceholderMessage();
		writePlaceHolderMessage(pm);
	}
	
	private void writePlaceHolderMessage(PlaceholderMessage message) throws Exception, IOException {

		String path = DESTINATION + IOUtils.DIR_SEPARATOR+message.getMessageID() + ".xml";
		
		FileWriter writer = new FileWriter();
		logger.debug("writing placeholdermesage to "+path);
		logger.trace("Message is: "+message);
		writer.writeObjectToFile(message, path);
	}
	
}
