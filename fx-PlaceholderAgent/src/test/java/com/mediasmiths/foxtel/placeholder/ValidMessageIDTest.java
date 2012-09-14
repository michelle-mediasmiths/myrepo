package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;

public class ValidMessageIDTest extends PlaceHolderMessageShortTest {

	private final static String EXISTING_MESSAGE_ID =  "EXISTING_MESSAGE_ID";
	
	public ValidMessageIDTest() throws JAXBException, SAXException, IOException {
		super();
	}
	
	/**
	 * confirms that a message with a reused message id is not considered valid 
	 * @throws IOException
	 * @throws Exception
	 */
	@Test
	public void testFileForWhichReceiptExistsFailsValidation() throws IOException, Exception{
		
		
		PurgeTitle pt = new PurgeTitle();
		pt.setTitleID(EXISTING_TITLE);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				pt);

		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(EXISTING_MESSAGE_ID);
		pm.setSenderID(SENDER_ID);
		pm.setActions(actions);
		
		File temp = createTempXMLFile(pm, "receiptExists");
		
		FileUtils.copyFile(temp, new File(receiptFolderPath + IOUtils.DIR_SEPARATOR +EXISTING_MESSAGE_ID + ".txt"));
		
		assertEquals(
				MessageValidationResult.INVALID_MESSAGE_ID,
				validator.validateFile(temp.getAbsolutePath()));
		
		
		
	}
	
}
