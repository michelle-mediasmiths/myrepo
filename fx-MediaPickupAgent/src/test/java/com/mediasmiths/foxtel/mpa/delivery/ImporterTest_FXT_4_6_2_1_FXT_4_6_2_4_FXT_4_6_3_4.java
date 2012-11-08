package com.mediasmiths.foxtel.mpa.delivery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.mpa.MaterialEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.ResultLogger;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.foxtel.mpa.processing.MarketingMaterialProcessingTest_FXT_4_6_3;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;

public class ImporterTest_FXT_4_6_2_1_FXT_4_6_2_4_FXT_4_6_3_4 {
	
	private static Logger logger = Logger.getLogger(ImporterTest_FXT_4_6_2_1_FXT_4_6_2_4_FXT_4_6_3_4.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	private PendingImportQueue pendingImports;
	private String incomingPath;
	private String archivePath;
	private String failurePath;
	private String ardomeImportPath;
	private Thread importerThread;
	private Importer toTest;
	private File media;
	private File materialxml;
	private String masterID;
	private MaterialEnvelope envelope;
	private PendingImport pendingImport;
	
	private int deliveryAttemptsToMake=2;
	private EventService event;

	@Before
	public void before() throws IOException {
		pendingImports = new PendingImportQueue();
		incomingPath = TestUtil.prepareTempFolder("INCOMING");
		archivePath = TestUtil.prepareTempFolder("ARCHIVE");
		failurePath = TestUtil.prepareTempFolder("FAILURE");
		ardomeImportPath = TestUtil.prepareTempFolder("ARDOMEIMPORT");

		event=mock(EventService.class);
		
		toTest = new Importer(pendingImports, ardomeImportPath, failurePath,
				archivePath,""+deliveryAttemptsToMake,event);
		
		importerThread = new Thread(toTest);
		importerThread.start();

		media = TestUtil.getFileOfTypeInFolder("mxf", incomingPath);
		TestUtil.writeBytesToFile(100, media);
		materialxml = TestUtil.getFileOfTypeInFolder("xml", incomingPath);
		TestUtil.writeBytesToFile(100, materialxml);
		masterID = "MASTERID";

		envelope = new MaterialEnvelope(materialxml, new Material(), masterID);
		pendingImport = new PendingImport(media, envelope);

		//Check if can write
			//Change from read only

		if (!new File(ardomeImportPath).canWrite()){
			new File(ardomeImportPath).setWritable(true);
		}

		
	}

	@After
	public void after() {
		importerThread.interrupt(); // kill importer thread
	}

	@Test
	public void testDelivery_FXT_4_6_2_1_FXT_4_6_2_4_FXT_4_6_3_4() throws IOException, InterruptedException {

		logger.info("Starting FXT 4.6.2.1/4.6.2.4/4.6.3_4  -  Media matched with placeholder (Part1)/Processed material exchange messages are archived");
		// add pending import to the queue
		pendingImports.add(pendingImport);
		// wait for some time to allow processing to take place
		Thread.sleep(1000l);

		// check the queue is now empty
		assertTrue(pendingImports.size() == 0);

		// check the files have been delivered to the expected folders
		Boolean mxfExists=new File(ardomeImportPath + IOUtils.DIR_SEPARATOR + masterID+ FilenameUtils.EXTENSION_SEPARATOR + "mxf").exists();
		assertTrue(mxfExists);
		
		Boolean xmlExists=new File(archivePath + IOUtils.DIR_SEPARATOR + masterID+ FilenameUtils.EXTENSION_SEPARATOR + "xml").exists();
		assertTrue(xmlExists);
		
		// check the files are no longer in the original folders
		Boolean mediaExists= media.exists();
		assertFalse(mediaExists);
		
		Boolean materialExists=materialxml.exists();
		assertFalse(materialExists);
		
		if (mxfExists && xmlExists && !materialExists &&!mediaExists)
		{
			resultLogger.info("FXT 4.6.2.1/4.6.2.4/4.6.3_4  -  Media matched with placeholder (Part1)/Processed material exchange messages are archived --Passed");
		}
		else
			resultLogger.info("FXT 4.6.2.1/4.6.2.4/4.6.3_4  -  Media matched with placeholder (Part1)/Processed material exchange messages are archived --Failed");

		
		
	}

	@Test
	public void testDeliveryFailure() throws InterruptedException {

		// make ardome import folder read only so delivery will fail
		new File(ardomeImportPath).setReadOnly();

		// add pending import to the queue
		pendingImports.add(pendingImport);
		// wait for some time to allow processing to take place
		Thread.sleep(1000l);

		// check the queue is now empty
		assertTrue(pendingImports.size() == 0);

		// check the files have been delivered to the expected folders
		assertTrue(new File(failurePath + IOUtils.DIR_SEPARATOR + masterID
				+ FilenameUtils.EXTENSION_SEPARATOR + "mxf").exists());
		assertTrue(new File(failurePath + IOUtils.DIR_SEPARATOR + masterID
				+ FilenameUtils.EXTENSION_SEPARATOR + "xml").exists());
		// check the files are no longer in the original folders
		assertFalse(media.exists());
		assertFalse(materialxml.exists());
		
		// check failure alert sent
		verify(event).saveEvent(eq("error"), any(String.class));

	}

}
