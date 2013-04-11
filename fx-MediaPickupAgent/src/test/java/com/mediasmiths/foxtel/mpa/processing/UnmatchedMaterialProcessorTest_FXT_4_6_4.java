package com.mediasmiths.foxtel.mpa.processing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.mediasmiths.foxtel.agent.WatchFolder;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.ResultLogger;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.mayam.MayamClient;

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
		
		WatchFolders wfs = new WatchFolders();
		WatchFolder wf = new WatchFolder(incomingFolderPath);
		wf.setDelivery(deliveryPath);
		wfs.add(wf);
		
		String failedMessagesPath = TestUtil.createSubFolder(incomingFolderPath, MessageProcessor.FAILUREFOLDERNAME); 
		
		
		String unmatchedMXFFileName = "UnmatchedProcesserTest"+RandomStringUtils.randomAlphabetic(10) + FilenameUtils.EXTENSION_SEPARATOR + "mxf";
		String unmatchedMXFPath = incomingFolderPath + IOUtils.DIR_SEPARATOR + unmatchedMXFFileName;
		logger.debug("Using "+unmatchedMXFPath);		
		IOUtils.write(RandomStringUtils.random(20), new FileOutputStream(new File(unmatchedMXFPath)));
		
		//prepare mock matchmatcher
		UnmatchedFile um = new UnmatchedFile(timeout+1, unmatchedMXFPath);
	
		//mock alert interface
		EventService events = mock(EventService.class);
				
		//run object being tested
		UnmatchedMaterialProcessor toTest = new UnmatchedMaterialProcessor(wfs,events);
		toTest.setMayamClient(mock(MayamClient.class));
		toTest.processUnmatchedMXF(new File(unmatchedMXFPath));
		
		Boolean mxfExists=new File(unmatchedMXFPath).exists();
		assertFalse(mxfExists); //mxf should have moved to ardome emergencey import folder
		
		Boolean fileMovedToDeliveryLocation=new File(deliveryPath + IOUtils.DIR_SEPARATOR + unmatchedMXFFileName).exists();
		assertTrue(fileMovedToDeliveryLocation);
		
		
		if (!mxfExists && fileMovedToDeliveryLocation)
		{
			resultLogger.info("FXT 4.6.4.1  - Media with no xml is moved to Viz Ardome emergency import folder --Passed");
		}
		else
		{
			resultLogger.info("FXT 4.6.4.1  - Media with no xml is moved to Viz Ardome emergency import folder --Failed");
		}
		
		
	}
			
}
