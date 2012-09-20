package com.mediasmiths.foxtel.mpa.delivery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.mpa.Util;
import com.medismiths.foxtel.mpa.MaterialEnvelope;
import com.medismiths.foxtel.mpa.PendingImport;
import com.medismiths.foxtel.mpa.delivery.Importer;
import com.medismiths.foxtel.mpa.queue.PendingImportQueue;

public class ImporterTest {

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

	@Before
	public void before() throws IOException {
		pendingImports = new PendingImportQueue();
		incomingPath = Util.prepareTempFolder("INCOMING");
		archivePath = Util.prepareTempFolder("ARCHIVE");
		failurePath = Util.prepareTempFolder("FAILURE");
		ardomeImportPath = Util.prepareTempFolder("ARDOMEIMPORT");

		toTest = new Importer(pendingImports, ardomeImportPath, failurePath,
				archivePath);
		importerThread = new Thread(toTest);
		importerThread.start();

		media = Util.getFileOfTypeInFolder("mxf", incomingPath);
		Util.writeBytesToFile(100, media);
		materialxml = Util.getFileOfTypeInFolder("xml", incomingPath);
		Util.writeBytesToFile(100, materialxml);
		masterID = "MASTERID";

		envelope = new MaterialEnvelope(materialxml, new Material(), masterID);
		pendingImport = new PendingImport(media, envelope);

	}

	@After
	public void after() {
		importerThread.interrupt(); // kill importer thread
	}

	@Test
	public void testDelivery() throws IOException, InterruptedException {

		// add pending import to the queue
		pendingImports.add(pendingImport);
		// wait for some time to allow processing to take place
		Thread.sleep(1000l);

		// check the queue is now empty
		assertTrue(pendingImports.size() == 0);

		// check the files have been delivered to the expected folders
		assertTrue(new File(ardomeImportPath + IOUtils.DIR_SEPARATOR + masterID
				+ FilenameUtils.EXTENSION_SEPARATOR + "mxf").exists());
		assertTrue(new File(archivePath + IOUtils.DIR_SEPARATOR + masterID
				+ FilenameUtils.EXTENSION_SEPARATOR + "xml").exists());
		// check the files are no longer in the original folders
		assertFalse(media.exists());
		assertFalse(materialxml.exists());
	}

	public void testDeliveryRetrying() {

		// TODO allow a configurable number of retries

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
		
		// TODO : test that apropriate failure notifications\tasks are sent\created

	}

}
