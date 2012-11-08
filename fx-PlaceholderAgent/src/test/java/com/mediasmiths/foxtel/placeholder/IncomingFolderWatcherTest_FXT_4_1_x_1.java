package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.messagetests.ResultLogger;

public class IncomingFolderWatcherTest_FXT_4_1_x_1 {

	private static Logger logger = Logger.getLogger(IncomingFolderWatcherTest_FXT_4_1_x_1.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	
	private File xml1;
	private File xml2;
	private File fileWeAreNotInterestedIn;
	private String watchFolderPath;
	private String watchFolderPathChanged;
	private File xml1Backup;
	private File xml2Backup;

	@Before
	public void before() throws IOException {
		watchFolderPath = TestUtil.prepareTempFolder("INCOMING");
		watchFolderPathChanged = TestUtil.prepareTempFolder("incomingBackup");

		xml1 = new File(watchFolderPath + IOUtils.DIR_SEPARATOR
				+ "XmlFile1"+RandomStringUtils.randomAlphabetic(10)
				+ FilenameUtils.EXTENSION_SEPARATOR + "xml");
		xml2 = new File(watchFolderPath + IOUtils.DIR_SEPARATOR
				+ "XmlFile2"+ RandomStringUtils.randomAlphabetic(10)
				+ FilenameUtils.EXTENSION_SEPARATOR + "xml");
		
		fileWeAreNotInterestedIn = new File(watchFolderPath + IOUtils.DIR_SEPARATOR
				+"IntermediateZipfile"+ RandomStringUtils.randomAlphabetic(10)
				+ FilenameUtils.EXTENSION_SEPARATOR + "zip");
		
		createInputFiles(watchFolderPathChanged);
	}
	
	public void createInputFiles(String folderName){
		
		//These are used to test the agents, will do nothing now but need to be run the AgentTest in src/main/resources
		xml1Backup = new File(folderName + IOUtils.DIR_SEPARATOR
				+ "XmlFile1"+ FilenameUtils.EXTENSION_SEPARATOR + "xml");
		xml2Backup = new File(folderName + IOUtils.DIR_SEPARATOR
				+ "XmlFile2"+ FilenameUtils.EXTENSION_SEPARATOR + "xml");
		
		
		try {
			writeRandomFile(xml1Backup);
			writeRandomFile(xml2Backup);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void writeRandomFile(File f) throws FileNotFoundException,
			IOException {
		
		logger.info("Writing data to "+f.getAbsolutePath());
		
		IOUtils.write(RandomStringUtils.randomAlphanumeric(20),
				new FileOutputStream(f));
	}

	@Test
	public void testExistingFilesAreQueued() throws FileNotFoundException,
			IOException {

		writeRandomFile(xml1);
		writeRandomFile(xml2);


		FilesPendingProcessingQueue queue = new FilesPendingProcessingQueue();
		DirectoryWatchingQueuer toTest = new DirectoryWatchingQueuer(queue,
				watchFolderPath);
		toTest.setSleepTime(100l);

		// start watcher
		Thread watcherThread = new Thread(toTest);
		watcherThread.start();

		// wait a short while, files should be queued almost immediately
		try {
			Thread.sleep(100l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// stop the watcher
		toTest.setContinueWatching(false);

		logger.debug(String.format("Queue size %d entries: %s ", queue.size(), StringUtils.join(queue.toArray(), ',')));
		
		// check results
		assertTrue(queue.size() == 2);
		assertTrue(queue.contains(xml1.getAbsolutePath()));
		assertTrue(queue.contains(xml2.getAbsolutePath()));

	}

	@Test
	public void testNewFilesAreQueued_FXT_4_1_x_1() throws FileNotFoundException, IOException {
		logger.info("Starting FXT_4_1_x_1  The XML file is discovered");
		FilesPendingProcessingQueue queue = new FilesPendingProcessingQueue();
		DirectoryWatchingQueuer toTest = new DirectoryWatchingQueuer(queue,
				watchFolderPath);
		
		logger.trace("testNewFilesAreQueued enter");
		toTest.setSleepTime(10l);
		toTest.setNotTouchedPeriod(10l);

		// start watcher
		Thread watcherThread = new Thread(toTest);
		logger.trace("testNewFilesAreQueued starting watcher");
		watcherThread.start();

		// wait a while to give the watcher a chance to start up
		try {
			logger.trace("testNewFilesAreQueued sleeping");
			Thread.sleep(1000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.trace("testNewFilesAreQueued woke up");
		logger.trace("testNewFilesAreQueued queue size is "+queue.size() );
		// queue should be empty at this point
		assertTrue(queue.size() == 0);

		// write files
		logger.trace("testNewFilesAreQueued writing files");
		writeRandomFile(xml1);
		writeRandomFile(xml2);

		// wait a longer while to give the watcher a change to pick up the files
		try {
			logger.trace("testNewFilesAreQueued sleeping for longer");
			Thread.sleep(10000l); //we need to wait for such a long time as java.nio.file.WatcherService is slooooow
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.trace("testNewFilesAreQueued woke up");

		// stop the watcher
		logger.trace("testNewFilesAreQueued stopping the watcher");
		toTest.setContinueWatching(false);

		// check results
		logger.trace("testNewFilesAreQueued queue size is "+queue.size() );
		assertEquals(2,queue.size());
		
		Boolean xml1Found=queue.contains(xml1.getAbsolutePath());
		assertTrue(xml1Found);
		
		Boolean xml2Found=queue.contains(xml2.getAbsolutePath());
		assertTrue(xml2Found);
		
		if(xml1Found && xml2Found)
		{
			resultLogger.info("FXT_4_1_x_1  The XML file is discovered --Passed for 4.1.1-11");
		}
		else
		{
			resultLogger.info("FXT_4_1_x_1  The XML file is discovered --Failed for 4.1.1-11");
		}

	}

	@Test
	public void testOtherFileTypesAreIgnored() throws FileNotFoundException,
			IOException {
		FilesPendingProcessingQueue queue = new FilesPendingProcessingQueue();
		DirectoryWatchingQueuer toTest = new DirectoryWatchingQueuer(queue,
				watchFolderPath);
		toTest.setSleepTime(100l);

		// start watcher
		Thread watcherThread = new Thread(toTest);
		watcherThread.start();

		// wait a while to give the watcher a chance to start up
		try {
			Thread.sleep(1000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// queue should be empty at this point
		assertTrue(queue.size() == 0);

		// write file
		writeRandomFile(fileWeAreNotInterestedIn);
		
		// wait a longer while to give the watcher a change to pick up the files
		try {
			Thread.sleep(10000l); //we need to wait for such a long time as java.nio.file.WatcherService is slooooow
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// stop the watcher
		toTest.setContinueWatching(false);

		// check results
		assertTrue(queue.size() == 0); //queue should still be empty
		
	}

}
