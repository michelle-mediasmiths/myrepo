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
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mediasmiths.foxtel.agent.WatchFolder;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.ResultLogger;
import com.mediasmiths.foxtel.mpa.TestUtil;

public class ImporterTest_FXT_4_6_2_1_FXT_4_6_2_4_FXT_4_6_3_4 {
	
	private static Logger logger = Logger.getLogger(ImporterTest_FXT_4_6_2_1_FXT_4_6_2_4_FXT_4_6_3_4.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	private String incomingPath;
	private String archivePath;
	private String failurePath;
	private String ardomeImportPath;
	private Importer toTest;
	private File media;
	private File materialxml;
	private String masterID;
	private MediaEnvelope envelope;
	private PendingImport pendingImport;
	private WatchFolders watchFolders;
	
	private int deliveryAttemptsToMake=2;
	private EventService event;

	@Before
	@SuppressWarnings("unchecked")
	public void before() throws IOException {
		incomingPath = TestUtil.prepareTempFolder("INCOMING");
		archivePath = TestUtil.createSubFolder(incomingPath, MessageProcessor.ARCHIVEFOLDERNAME);
		failurePath = TestUtil.createSubFolder(incomingPath, MessageProcessor.FAILUREFOLDERNAME);
		ardomeImportPath = TestUtil.prepareTempFolder("ARDOMEIMPORT");
		WatchFolder wf = new WatchFolder(incomingPath);
		wf.setDelivery(ardomeImportPath);
		
		watchFolders = new WatchFolders();
		watchFolders.add(wf);
		
		event=mock(EventService.class);
		
		toTest = new Importer(watchFolders,
				""+deliveryAttemptsToMake,event);
		
		String rString = RandomStringUtils.randomAlphabetic(6);
		media = TestUtil.getFileOfTypeInFolder("mxf", incomingPath,rString);
		TestUtil.writeBytesToFile(100, media);
		materialxml = TestUtil.getFileOfTypeInFolder("xml", incomingPath,rString);
		TestUtil.writeBytesToFile(100, materialxml);
		masterID = "MASTERID";

		PickupPackage pp = new PickupPackage("xml","mxf");
		pp.addPickUp(media);
		pp.addPickUp(materialxml);
		
		envelope = new MediaEnvelope(pp, new Material(), masterID);
		pendingImport = new PendingImport(envelope);

		//Check if can write
			//Change from read only

		if (!new File(ardomeImportPath).canWrite()){
			new File(ardomeImportPath).setWritable(true);
		}

		
	}

	@After
	public void after() {
	}

	@Test
	public void testDelivery_FXT_4_6_2_1_FXT_4_6_2_4_FXT_4_6_3_4() throws IOException, InterruptedException {

		logger.info("Starting FXT 4.6.2.1/4.6.2.4/4.6.3_4  -  Media matched with placeholder (Part1)/Processed material exchange messages are archived");
		// add pending import to the queue
		
		PickupPackage pp = new PickupPackage("xml","mxf");
		pp.addPickUp(media);
		pp.addPickUp(materialxml);
		MediaEnvelope<Material> me = new MediaEnvelope<Material>(pp, new Material(),"masterid");
		PendingImport pi = new PendingImport(me);
		toTest.deliver(pi, 1);
		
		// wait for some time to allow processing to take place
		Thread.sleep(1000l);

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

		PickupPackage pp = new PickupPackage("xml","mxf");
		pp.addPickUp(media);
		pp.addPickUp(materialxml);
		MediaEnvelope<Material> me = new MediaEnvelope<Material>(pp, new Material(),"masterid");
		PendingImport pi = new PendingImport(me);
		toTest.deliver(pi, 1);
		
		// wait for some time to allow processing to take place
		Thread.sleep(1000l);

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
