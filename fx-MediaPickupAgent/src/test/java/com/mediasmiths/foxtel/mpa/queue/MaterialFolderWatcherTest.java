package com.mediasmiths.foxtel.mpa.queue;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.foxtel.mpa.queue.MaterialFolderWatcher;

public class MaterialFolderWatcherTest {

	private static Logger logger = Logger.getLogger(MaterialFolderWatcherTest.class);
	
	private File xml1;
	private File xml2;
	private File mxf1;
	private File mxf2;
	private File fileWeAreNotInterestedIn;
	private String watchFolderPath;

	@Before
	public void before() throws IOException {
		watchFolderPath = TestUtil.prepareTempFolder("INCOMING");

		xml1 = new File(watchFolderPath + IOUtils.DIR_SEPARATOR
				+ RandomStringUtils.randomAlphabetic(10)
				+ FilenameUtils.EXTENSION_SEPARATOR + "xml");
		xml2 = new File(watchFolderPath + IOUtils.DIR_SEPARATOR
				+ RandomStringUtils.randomAlphabetic(10)
				+ FilenameUtils.EXTENSION_SEPARATOR + "xml");

		mxf1 = new File(watchFolderPath + IOUtils.DIR_SEPARATOR
				+ RandomStringUtils.randomAlphabetic(10)
				+ FilenameUtils.EXTENSION_SEPARATOR + "mxf");
		mxf2 = new File(watchFolderPath + IOUtils.DIR_SEPARATOR
				+ RandomStringUtils.randomAlphabetic(10)
				+ FilenameUtils.EXTENSION_SEPARATOR + "mxf");
		
		fileWeAreNotInterestedIn = new File(watchFolderPath + IOUtils.DIR_SEPARATOR
				+ RandomStringUtils.randomAlphabetic(10)
				+ FilenameUtils.EXTENSION_SEPARATOR + "zip");
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
		writeRandomFile(mxf1);
		writeRandomFile(mxf2);

		FilesPendingProcessingQueue queue = new FilesPendingProcessingQueue();
		MaterialFolderWatcher toTest = new MaterialFolderWatcher(queue,
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

		// check results
		assertTrue(queue.size() == 4);
		assertTrue(queue.contains(xml1.getAbsolutePath()));
		assertTrue(queue.contains(xml2.getAbsolutePath()));
		assertTrue(queue.contains(mxf1.getAbsolutePath()));
		assertTrue(queue.contains(mxf2.getAbsolutePath()));

	}

	@Test
	public void testNewFilesAreQueued() throws FileNotFoundException, IOException {
		FilesPendingProcessingQueue queue = new FilesPendingProcessingQueue();
		MaterialFolderWatcher toTest = new MaterialFolderWatcher(queue,
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

		// write files
		writeRandomFile(xml1);
		writeRandomFile(xml2);
		writeRandomFile(mxf1);
		writeRandomFile(mxf2);

		// wait a longer while to give the watcher a change to pick up the files
		try {
			Thread.sleep(10000l); //we need to wait for such a long time as java.nio.file.WatcherService is slooooow
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// stop the watcher
		toTest.setContinueWatching(false);

		// check results
		assertTrue(queue.size() == 4);
		assertTrue(queue.contains(xml1.getAbsolutePath()));
		assertTrue(queue.contains(xml2.getAbsolutePath()));
		assertTrue(queue.contains(mxf1.getAbsolutePath()));
		assertTrue(queue.contains(mxf2.getAbsolutePath()));
	}

	@Test
	public void testOtherFileTypesAreIgnored() throws FileNotFoundException,
			IOException {
		FilesPendingProcessingQueue queue = new FilesPendingProcessingQueue();
		MaterialFolderWatcher toTest = new MaterialFolderWatcher(queue,
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
