package com.mediasmiths.foxtel.placeholder.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.PlaceholderManagerTest;
import com.mediasmiths.foxtel.placeholder.TestUtil;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.TestAddOrUpdateMaterial;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class ValidationFailedMessageMovesToFailureFolderTest extends
		PlaceholderManagerTest {

	private static Logger logger = Logger
			.getLogger(ValidationFailedMessageMovesToFailureFolderTest.class);

	public ValidationFailedMessageMovesToFailureFolderTest()
			throws JAXBException, SAXException {
		super();
	}

	@Test
	public void testInvalidMessageMovesToFailureFolder() throws IOException,
			InterruptedException {
		// prepare folders
		String messagePath = Util.prepareTempFolder("MESSAGE");
		String receiptPath = TestUtil.createSubFolder(messagePath, MessageProcessor.ARCHIVEFOLDERNAME );
		String failurePath = TestUtil.createSubFolder(messagePath, MessageProcessor.FAILUREFOLDERNAME);
		String archivePath = TestUtil.createSubFolder(messagePath, MessageProcessor.ARCHIVEFOLDERNAME );
	

		// prepare message
		String message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur hendrerit consequat enim a vestibulum. Mauris at mauris ac magna auctor hendrerit varius accumsan est. Duis nisl risus, tempor non hendrerit sed, vestibulum non sem. Sed et erat quis urna venenatis tincidunt. Curabitur sit amet mauris felis. Proin sagittis sem id eros ornare sit amet faucibus ipsum molestie. Phasellus laoreet pellentesque odio vel feugiat. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc sed libero dolor.";
		String messageFilePath = messagePath + IOUtils.DIR_SEPARATOR
				+ "INVALIDMESSAGE.xml";

		// write message
		logger.debug("Writing message to " + messageFilePath);
		IOUtils.write(message, new FileOutputStream(new File(messageFilePath)));

		// run manager
		runPlaceholderManager(messagePath);

		// after manager has run and processed the single task the request
		// should now be in the failure folder
		File failFile = new File(failurePath + IOUtils.DIR_SEPARATOR
				+ FilenameUtils.getName(messageFilePath));
		File messageFile = new File(messageFilePath);

		logger.info("Looking for " + failFile.getAbsolutePath());
		assertTrue(failFile.exists());
		logger.info("Looking for " + messageFile.getAbsolutePath());
		assertFalse(messageFile.exists());
		
		Util.deleteFiles(messagePath,receiptPath,failurePath,archivePath);
	}

	@Test
	public void testValidFormattedMessageForWhichValidationFailsMovesToFailureFolder()
			throws JAXBException, SAXException, Exception {

		// prepare folders
		String messagePath = Util.prepareTempFolder("MESSAGE");
		String receiptPath = TestUtil.createSubFolder(messagePath, MessageProcessor.ARCHIVEFOLDERNAME );
		String failurePath = TestUtil.createSubFolder(messagePath, MessageProcessor.FAILUREFOLDERNAME);
		String archivePath = TestUtil.createSubFolder(messagePath, MessageProcessor.ARCHIVEFOLDERNAME );
	
logger.info("message path "+messagePath);
logger.info("receiptPath "+receiptPath);
logger.info("failurePath "+failurePath);
logger.info("archivePath "+archivePath);
		
		String filename = "validMessage.xml";
		String messageFilePath = messagePath + IOUtils.DIR_SEPARATOR
				+ "validMessage.xml";

		// prepare message
		PlaceholderMessage message = new TestAddOrUpdateMaterial()
				.generatePlaceholderMessage();

		// prepare mock mayamClient to cause a failure when querying if a
		// material exists
		when(mayamClient.titleExists(anyString())).thenThrow(
				new MayamClientException(
						MayamClientErrorCode.MATERIAL_FIND_FAILED));

		// run manager
		writeMessageAndRunManager(message, messagePath, filename);

		// after manager has run and processed the single task the request
		// should now be in the failure folder
		File failFile = new File(failurePath + IOUtils.DIR_SEPARATOR
				+ FilenameUtils.getName(messageFilePath));
		File messageFile = new File(messageFilePath);

		logger.info("Looking for " + failFile.getAbsolutePath());
		assertTrue(failFile.exists());
		logger.info("Looking for " + messageFile.getAbsolutePath());
		assertFalse(messageFile.exists());
		
		Util.deleteFiles(messagePath,receiptPath,failurePath,archivePath);
	}

}
