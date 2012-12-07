package com.mediasmiths.foxtel.mpa.validation;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mediasmiths.foxtel.generated.MaterialExchange.FileMediaType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;
import com.mediasmiths.foxtel.mpa.ProgrammeMaterialTest;
import com.mediasmiths.foxtel.mpa.ResultLogger;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.foxtel.mpa.Util;

@Ignore //not checking file size and checksume against details in the materialxml
public class MediaCheckTest_FXT_4_6_1 {
	
	private static Logger logger = Logger.getLogger(MediaCheckTest_FXT_4_6_1.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	final String fileContents = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eros ipsum, rhoncus vel pulvinar id, vulputate nec sem. Vestibulum sollicitudin dui quis enim posuere interdum. Proin sed augue vitae felis facLorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eros ipsum, rhoncus vel pulvinar id, vulputate nec sem. Vestibulum sollicitudin dui quis enim posuere interdum. Proin sed augue vitae felis facilisis varius. Cras laoreet placerat justo, ac varius velit porta sed. Nulla viverra ante in odio lacinia vitae porttitor diam dignissim. Aliquam vehicula porttitor euismod. Duis neque libero, egestas eget vulputate in, egestas nec odio.\n\nNulla facilisi. Fusce ut ligula at nibh bibendum dignissim. Pellentesque pharetra metus ac dolor vestibulum tempus. Morbi eget lacus eget magna lacinia vehicula. Quisque bibendum faucibus libero nec fringilla. Vestibulum consectetur orci sit amet mi dictum a pulvinar mi facilisis. Nullam risus quam, auctor non consequat nec, dapibus vehicula lacus. Mauris nisi sapien, sodales vitae malesuada vel, porttitor id mauris. Integer lacus elit, feugiat sodales aliquet quis, egestas in felis. Maecenas interdum turpis sit amet tellus feugiat ut tristique metus hendrerit. Aenean sodales turpis at turpis venenatis nec vestibulum justo mollis.\n\nSed nibh sapien, luctus eu adipiscing nec, dignissim nec metus. Duis felis dui, interdum ullamcorper commodo sed, posuere nec tortor. Aliquam nec lectus ante. Donec rhoncus vehicula tincidunt. Curabitur libero est, eleifend eu fermentum eget, consectetur et nibh. Vivamus mollis tempus est. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Nullam placerat porta diam, sagittis tempor massa dictum eu.\n\nDuis eget diam quis nisl cursus congue. Nam posuere dignissim tellus vitae imperdiet. Quisque in magna purus, vitae fringilla ligula. Etiam sit amet massa dolor, ac sagittis libero. Cras tempor sodales mauris, ut pellentesque dolor dignissim ut. Vivamus faucibus, eros sed faucibus rhoncus, nibh est rutrum sapien, id pretium nisi libero eu est. Quisque venenatis dapibus augue, pulvinar adipiscing dolor feugiat ac. Nulla auctor massa ac augue hendrerit faucibus. Aliquam ornare posuere diam eu pulvinar.\n\nSed suscipit, justo ultrices tincidunt luctus, purus nunc elementum mauris, et sodales mi ligula sit amet massa. Curabitur aliquet, velit sit amet tristique ornare, augue lacus tincidunt mauris, vitae volutpat eros velit ac ante. Praesent rhoncus erat vitae elit semper sollicitudin. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; In hac habitasse platea dictumst. Curabitur varius sem ac lacus semper lacinia. Vivamus faucibus, sapien et posuere suscipit, nibh tortor blandit felis, vitae egestas mi lacus dapibus nulla. Nam sodales, dolor quis convallis pellentesque, tellus sem tempus risus, sit amet lacinia dolor lectus quis felis. Aliquam sit amet congue arcu. Cras in dapibus lacus. Morbi laoreet, metus euismod sollicitudin malesuada, mauris lorem rhoncus felis, id tincidunt orci magna ac quam. Nullam vehicula lectus a nisl luctus nec sagittis ligula eleifend. Fusce enim mi, rhoncus vitae laoreet sit amet, cursus vel ante. Suspendisse ultrices semper urna in varius. Praesent accumsan erat nisi.";
	File mxf;

	@Before
	public void before() throws IOException {

		String incomingPath = TestUtil.prepareTempFolder("INCOMING");
		mxf = TestUtil.getFileOfTypeInFolder("mxf", incomingPath);
		IOUtils.write(fileContents, new FileOutputStream(mxf));
	}

	// test for file sizes and + checksums matching - media check should pass
	@Test
	public void testValidMedia_FXT_4_6_1_3() throws IOException,
			DatatypeConfigurationException {
		String testName="FXT 4.6.1.3  - Media arrives with expected checksum and expected file size";
		logger.info("Starting" +testName);

		long expectedSize = fileContents.getBytes().length;
		// calculated by textutils md5sum
		String expectedMd5sum = "da75f0c8711c0ec5ca5c2e0c11a8ac11";
		doTest(expectedSize, expectedMd5sum, true, testName);

	}

	protected void doTest(long expectedSize, String expectedMd5sum,
			boolean shouldPass, String testName) throws IOException,
			DatatypeConfigurationException {

		Material material = ProgrammeMaterialTest.getMaterialNoPackages(
				"TITLE", "MATERIAL");
		MaterialType materialType = Util.getMaterialTypeForMaterial(material);
		FileMediaType media = (FileMediaType) materialType.getMedia();
		media.setFileSize(new BigInteger("" + expectedSize));
		media.setChecksum(new BigInteger(expectedMd5sum, 16));

		MediaCheck toTest = new MediaCheck();
		Boolean intermediate= toTest.mediaCheck(mxf, new MediaEnvelope(new File(""), material));
		if (testName != null)
		{
			if(shouldPass == intermediate)
				resultLogger.info(testName + " --Passed");
			else
				resultLogger.info(testName + " --Failed");
		}	

		assertTrue(shouldPass == intermediate);
	}

	// test for file sizes not matching but checksums do - media check should
	// fail
	@Test
	public void testInvalidMediaWrongFileSize_FXT_4_6_1_1() throws IOException,
			DatatypeConfigurationException {
		String testName="FXT 4.6.1.1  - Media arrives with unexpected file size";
		logger.info("Starting" +testName);


		long expectedSize = fileContents.getBytes().length + 10;
		// calculated by textutils md5sum
		String expectedMd5sum = "da75f0c8711c0ec5ca5c2e0c11a8ac11";
		doTest(expectedSize, expectedMd5sum, false, testName);
	}

	// test for file sizes matching but checksums not - media check should fail

	@Test
	public void testInvalidMediaWrongCheckSum_FXT_4_6_1_2() throws IOException,
			DatatypeConfigurationException {
		String testName="FXT 4.6.1.2  - Media arrives with unexpected checksum";
		logger.info("Starting" + testName);

		long expectedSize = fileContents.getBytes().length;
		String expectedMd5sum = RandomStringUtils.random(32, new char[] { 'a',
				'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3', '4', '5', '6',
				'7', '8', '9' });
		doTest(expectedSize, expectedMd5sum, false, testName);
	}

	@Test
	public void testIOExceptionResultsInMediaCheckFailure() throws IOException,
			DatatypeConfigurationException {

		// make file unreadable
		mxf.setReadable(false);

		long expectedSize = fileContents.getBytes().length;
		// calculated by textutils md5sum
		String expectedMd5sum = "da75f0c8711c0ec5ca5c2e0c11a8ac11";
		doTest(expectedSize, expectedMd5sum, false, null);

	}

}
