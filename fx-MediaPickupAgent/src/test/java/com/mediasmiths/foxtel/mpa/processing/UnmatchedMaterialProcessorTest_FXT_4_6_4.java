package com.mediasmiths.foxtel.mpa.processing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.mediasmiths.foxtel.agent.WatchFolder;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;
import com.mediasmiths.foxtel.mpa.ResultLogger;
import com.mediasmiths.foxtel.mpa.TestUtil;

public class UnmatchedMaterialProcessorTest_FXT_4_6_4 {

	//the time from when an unmatched file is seen until we stop waiting for its partner to arrive
	protected final static long timeout = 100l;
	protected final static long timebetweenpurges = 100l;
	
	private static Logger logger = Logger
			.getLogger(UnmatchedMaterialProcessorTest_FXT_4_6_4.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);


	@Test
	public void testUnmatchedFilesMoveToApropriateFolder_FXT_4_6_4_1() throws IOException, InterruptedException{
		logger.info( "Starting FXT 4.6.4.1  - Media with no xml is moved to Viz Ardome emergency import folder");

		
		//prepare folders and write unmatched xml file
		String incomingFolderPath = TestUtil.prepareTempFolder("INCOMING");		
		String archivePath =	TestUtil.createSubFolder(incomingFolderPath, MessageProcessor.ARCHIVEFOLDERNAME);
		String failurePath = TestUtil.createSubFolder(incomingFolderPath, MessageProcessor.FAILUREFOLDERNAME);
		String deliveryPath =  TestUtil.prepareTempFolder("DELIVERY");
//		String emergencyFolderPath = TestUtil.prepareTempFolder("ARDOMEEMERGENCYIMPORT");
		
		WatchFolders wfs = new WatchFolders();
		WatchFolder wf = new WatchFolder(incomingFolderPath);
		wf.setDelivery(deliveryPath);
		wfs.add(wf);
		
		String failedMessagesPath = TestUtil.createSubFolder(incomingFolderPath, MessageProcessor.FAILUREFOLDERNAME); 
		
		String unmatchedXMlFileName = "UnmatchedProcesserTest"+RandomStringUtils.randomAlphabetic(10) + FilenameUtils.EXTENSION_SEPARATOR + "xml";
		String unmatchedXMLPath = incomingFolderPath + IOUtils.DIR_SEPARATOR + unmatchedXMlFileName;
		logger.debug("Using "+unmatchedXMLPath);		
		IOUtils.write(RandomStringUtils.random(20), new FileOutputStream(new File(unmatchedXMLPath)));
		
		String unmatchedMXFFileName = "UnmatchedProcesserTest"+RandomStringUtils.randomAlphabetic(10) + FilenameUtils.EXTENSION_SEPARATOR + "mxf";
		String unmatchedMXFPath = incomingFolderPath + IOUtils.DIR_SEPARATOR + unmatchedMXFFileName;
		logger.debug("Using "+unmatchedMXFPath);		
		IOUtils.write(RandomStringUtils.random(20), new FileOutputStream(new File(unmatchedMXFPath)));
		
		//prepare mock matchmatcher
		MatchMaker mm = mock(MatchMaker.class);
		UnmatchedFile um = new UnmatchedFile(timeout+1, unmatchedMXFPath);
		when(mm.purgeUnmatchedMessages(timeout)).
			thenReturn(Collections.<MediaEnvelope>singletonList(new MediaEnvelope(new File(unmatchedXMLPath), null))).
			thenReturn(Collections.<MediaEnvelope>emptyList());
		when(mm.purgeUnmatchedMXFs(timeout)).
			thenReturn(Collections.<UnmatchedFile>singletonList(um)).
			thenReturn(Collections.<UnmatchedFile>emptyList());
		
		//mock alert interface
		EventService events = mock(EventService.class);
				
		//run object being tested
		UnmatchedMaterialProcessor toTest = new UnmatchedMaterialProcessor(timeout,timebetweenpurges,wfs,mm,events);
		Thread unmatcherThread = new Thread(toTest);
		unmatcherThread.start();
		
		//wait a while for processing to take place
		Thread.sleep(500l);
		//stop the unmatched material processor
		unmatcherThread.interrupt();
		
		//check results		
		verify(mm, atLeastOnce()).purgeUnmatchedMessages(timeout);
		verify(mm, atLeastOnce()).purgeUnmatchedMXFs(timeout);
		
		Boolean xmlExists=new File(unmatchedXMLPath).exists();
		assertFalse(xmlExists); //message should have been moved to the failed folder
		
		Boolean mxfExists=new File(unmatchedMXFPath).exists();
		assertFalse(mxfExists); //mxf should have moved to ardome emergencey import folder
		
		Boolean fileMovedToFailed=new File(failedMessagesPath + IOUtils.DIR_SEPARATOR + unmatchedXMlFileName).exists();
		assertTrue(fileMovedToFailed);
		
		Boolean fileMovedToDeliveryLocation=new File(deliveryPath + IOUtils.DIR_SEPARATOR + unmatchedMXFFileName).exists();
		assertTrue(fileMovedToDeliveryLocation);
		
		
		if (!mxfExists && !xmlExists && fileMovedToDeliveryLocation && fileMovedToFailed)
		{
			resultLogger.info("FXT 4.6.4.1  - Media with no xml is moved to Viz Ardome emergency import folder --Passed");
		}
		else
		{
			resultLogger.info("FXT 4.6.4.1  - Media with no xml is moved to Viz Ardome emergency import folder --Failed");
		}
		
		
	}
			
}
