package com.mediasmiths.foxtel.placeholder.validmessagepickup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.placeholder.PlaceholderManagerTest;
import com.mediasmiths.foxtel.placeholder.categories.MessageCreation;
import com.mediasmiths.foxtel.placeholder.categories.PickuptoFailure;
import com.mediasmiths.foxtel.placeholder.categories.PickuptoReceipt;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidationResult;

public abstract class ValidMessagePickTest extends PlaceholderManagerTest {

	public ValidMessagePickTest() throws JAXBException, SAXException {
		super();
	}

	private static Logger logger = Logger.getLogger(ValidMessagePickTest.class);
	
	protected abstract PlaceholderMessage generatePlaceholderMessage () throws Exception;
	protected abstract String getFileName();
	
	protected String getFilePath() throws IOException{
		return "/tmp" + IOUtils.DIR_SEPARATOR + getFileName();
	}
	
		
	protected abstract void mockValidCalls(PlaceholderMessage mesage) throws Exception;
	protected abstract void verifyValidCalls(PlaceholderMessage message) throws Exception;
	protected abstract void mockInValidCalls(PlaceholderMessage mesage) throws Exception;
	protected abstract void verifyInValidCalls(PlaceholderMessage message) throws Exception;
	
	protected Object getAction(PlaceholderMessage message){
		return message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
	}
	
	
	@Test
	@Category(MessageCreation.class)
	public final void testWrittenPlaceHolderMessagesValidate () throws Exception {
		String filePath = getFilePath();
		PlaceholderMessage message = this.generatePlaceholderMessage();
		writePlaceHolderMessage(message,filePath);
		when(receiptWriter.receiptPathForMessageID(anyString())).thenReturn("/tmp/"+RandomStringUtils.randomAlphabetic(30));
		mockValidCalls(message);
		//test that the generated placeholder message is valid
		assertEquals(MessageValidationResult.IS_VALID,validator.validateFile(filePath));
		
	}
	
	@Test
	@Category(PickuptoFailure.class)
	/**
	 * Tests that valid messages whos processing fails get moved to the failure folder
	 * 
	 * Extending classes should provide implementations of mockInValidCalls (which should at some point cause a failure) and verifyInValidCalls 
	 * 
	 * @throws Exception
	 */
	public final void testValidRequestThatFailsProcessesToFailure() throws Exception{
		
		String messagePath = Util.prepareTempFolder("MESSAGE");
		String receiptPath = Util.prepareTempFolder("RECEIPT");
		String failurePath = Util.prepareTempFolder("FAILURE");
			
		PlaceholderMessage message = this.generatePlaceholderMessage();
		mockInValidCalls(message);
		
		String messageFilePath = messagePath + getFileName();				
		writeMessageAndRunManager(message,messagePath,receiptPath,failurePath,messageFilePath);

		verifyInValidCalls(message);
		
		//after manager has run and processed the single task the request should now be in the failure folder
		File failFile = new File(failurePath + IOUtils.DIR_SEPARATOR + FilenameUtils.getName(messageFilePath));
		File messageFile = new File(messageFilePath);
		logger.info("Looking for "+failFile.getAbsolutePath());
		assertTrue(failFile.exists());
		logger.info("Looking for "+messageFile.getAbsolutePath());
		assertFalse(messageFile.exists());
		
	}
	
	
	@Test
	@Category(PickuptoReceipt.class)
	/**
	 * Tests that a valid request gets processed an a receipt placed in the receipts folder, extending classes should implement mockValidCalls and verifyValid calls to guide  and validate the tests
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	public final void testValidRequestProcessesToReceipt() throws IOException, Exception{
		
		String messagePath = Util.prepareTempFolder("MESSAGE");
		String receiptPath = Util.prepareTempFolder("RECEIPT");
		String failurePath = Util.prepareTempFolder("FAILURE");
		
		PlaceholderMessage message = this.generatePlaceholderMessage();
		mockValidCalls(message);
		
		String messageFilePath = getFilePath();
		writeMessageAndRunManager(message, messagePath, receiptPath, failurePath,messageFilePath);

		verifyValidCalls(message);
		
		//after manager has run and processed the single task we should have a receipt		
		File receiptFile = new File(receiptPath + IOUtils.DIR_SEPARATOR +  message.getMessageID() + ".txt");	
		logger.info("Looking for "+receiptFile.getAbsolutePath());
		assertTrue(receiptFile.exists());
		
		//TODO what happens to the xml in the message folder after is has finished processing?
		
		
	}
	
	
}
