package com.mediasmiths.foxtel.mpa.processing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.mediasmiths.foxtel.mpa.Util;
import com.medismiths.foxtel.mpa.MaterialEnvelope;
import com.medismiths.foxtel.mpa.processing.MatchMaker;
import com.medismiths.foxtel.mpa.processing.UnmatchedFile;
import com.medismiths.foxtel.mpa.processing.UnmatchedMaterialProcessor;

public class UnmatchedMaterialProcessorTest {

	//the time from when an unmatched file is seen until we stop waiting for its partner to arrive
	protected final static long timeout = 100l;
	protected final static long timebetweenpurges = 100l;
	
	private static Logger logger = Logger
			.getLogger(UnmatchedMaterialProcessorTest.class);

	@Test
	public void testUnmatchedFilesMoveToApropriateFolder() throws IOException, InterruptedException{
		
		//prepare folders and write unmatched xml file
		String incomingFolderPath = Util.prepareTempFolder("INCOMING");		
		String emergencyFolderPath = Util.prepareTempFolder("ARDOMEEMERGENCYIMPORT");
		String failedMessagesPath = Util.prepareTempFolder("FAILED");
		
		String unmatchedXMlFileName = RandomStringUtils.randomAlphabetic(10) + FilenameUtils.EXTENSION_SEPARATOR + "xml";
		String unmatchedXMLPath = incomingFolderPath + IOUtils.DIR_SEPARATOR + unmatchedXMlFileName;
		logger.debug("Using "+unmatchedXMLPath);		
		IOUtils.write(RandomStringUtils.random(20), new FileOutputStream(new File(unmatchedXMLPath)));
		
		String unmatchedMXFFileName = RandomStringUtils.randomAlphabetic(10) + FilenameUtils.EXTENSION_SEPARATOR + "mxf";
		String unmatchedMXFPath = incomingFolderPath + IOUtils.DIR_SEPARATOR + unmatchedMXFFileName;
		logger.debug("Using "+unmatchedMXFPath);		
		IOUtils.write(RandomStringUtils.random(20), new FileOutputStream(new File(unmatchedMXFPath)));
		
		//prepare mock matchmatcher
		MatchMaker mm = mock(MatchMaker.class);
		UnmatchedFile um = new UnmatchedFile(timeout+1, unmatchedMXFPath);
		when(mm.purgeUnmatchedMessages(timeout)).
			thenReturn(Collections.<MaterialEnvelope>singletonList(new MaterialEnvelope(new File(unmatchedXMLPath), null))).
			thenReturn(Collections.<MaterialEnvelope>emptyList());
		when(mm.purgeUnmatchedMXFs(timeout)).
			thenReturn(Collections.<UnmatchedFile>singletonList(um)).
			thenReturn(Collections.<UnmatchedFile>emptyList());
		
		//run object being tested
		UnmatchedMaterialProcessor toTest = new UnmatchedMaterialProcessor(timeout,timebetweenpurges, emergencyFolderPath, failedMessagesPath, mm);
		Thread unmatcherThread = new Thread(toTest);
		unmatcherThread.start();
		
		//wait a while for processing to take place
		Thread.sleep(500l);
		//stop the unmatched material processor
		toTest.stop();
		
		//check results		
		verify(mm, atLeastOnce()).purgeUnmatchedMessages(timeout);
		verify(mm, atLeastOnce()).purgeUnmatchedMXFs(timeout);
		
		assertFalse(new File(unmatchedXMLPath).exists()); //message should have been moved to the failed folder
		assertFalse(new File(unmatchedMXFPath).exists()); //mxf should have moved to ardome emergencey import folder
		assertTrue(new File(failedMessagesPath + IOUtils.DIR_SEPARATOR + unmatchedXMlFileName).exists());
		assertTrue(new File(emergencyFolderPath + IOUtils.DIR_SEPARATOR + unmatchedMXFFileName).exists());
	}
	
	@Test
	public void testNotificationsFireForUmatchedMaterial(){
		//TODO implement this test. When unmatched material is discovered then someone needs to be notified
	}
		
}
