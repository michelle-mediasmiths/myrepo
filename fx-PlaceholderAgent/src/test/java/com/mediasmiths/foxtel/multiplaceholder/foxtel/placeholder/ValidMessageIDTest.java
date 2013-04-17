package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Ignore
public class ValidMessageIDTest extends PlaceHolderMessageShortTest
{

	private final static String EXISTING_MESSAGE_ID =  "EXISTING_MESSAGE_ID";

	public ValidMessageIDTest() throws JAXBException, SAXException, IOException {
		super();
	}

	/**
	 * confirms that a message with a reused message id does not fail validation because of it
	 * @throws java.io.IOException
	 * @throws Exception
	 */
	@Test
	public void testFileForWhichReceiptExistsStillValidates() throws IOException, Exception{
		
		
		PurgeTitle pt = new PurgeTitle();
		pt.setTitleID(EXISTING_TITLE);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				pt);

		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(EXISTING_MESSAGE_ID);
		pm.setSenderID(createSenderID());
		pm.setActions(actions);
		
		PickupPackage pp = createTempXMLFile (pm, "receiptExists");
		
		FileUtils.copyFile(pp.getPickUp("xml"), new File(receiptFolderPath + IOUtils.DIR_SEPARATOR +EXISTING_MESSAGE_ID + ".txt"));
		
		assertEquals(
				MessageValidationResult.NO_EXISTING_TITLE_TO_PURGE,
				validator.validatePickupPackage(pp));
		Util.deleteFiles(pp);
	}
	
}
