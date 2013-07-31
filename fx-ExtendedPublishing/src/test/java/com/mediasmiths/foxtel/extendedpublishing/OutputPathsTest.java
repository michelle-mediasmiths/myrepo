package com.mediasmiths.foxtel.extendedpublishing;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.std.io.PropertyFile;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OutputPathsTest
{
	protected final static Logger log = Logger.getLogger(OutputPathsTest.class);


	@Before
	public void before()
	{

		Injector injector = Guice.createInjector(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				PropertyFile properties = PropertyFile.find("environment.properties");
				Names.bindProperties(this.binder(), properties.toProperties());

				bind(ChannelProperties.class).to(ChannelPropertiesStub.class);
			}
		});
		// get instance of class to test
		toTest = injector.getInstance(OutputPaths.class);
	}


	@Inject
	OutputPaths toTest;


	@Test
	public void testPublicityFTPPath()
	{
		String expected = "exports/Exports/General Entertainment/Publicity";
		String actual = toTest.getFTPPathToExportDestinationFolder("FOX", TranscodeJobType.PUBLICITY_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testPublicityLocalPath()
	{
		String expected = "/storage/corp/exports/Exports/General Entertainment/Publicity";
		String actual = toTest.getLocalPathToExportDestinationFolder("FOX", TranscodeJobType.PUBLICITY_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testPublicityLocalFullPath()
	{
		String expected = "/storage/corp/exports/Exports/General Entertainment/Publicity/export.wmv";
		String actual = toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.PUBLICITY_PROXY, "export", false);
		assertEquals(expected, actual);
	}


	@Test
	public void testPublicityLocalFullPathDVD()
	{
		String expected = "/storage/corp/exports/Exports/General Entertainment/Publicity/export";
		String actual = toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.PUBLICITY_PROXY, "export", true);
		assertEquals(expected, actual);
	}


	@Test
	public void testComplianceFTPPath()
	{
		String expected = "exports/Exports/General Entertainment/Compliance";
		String actual = toTest.getFTPPathToExportDestinationFolder("FOX", TranscodeJobType.COMPLIANCE_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testComplianceLocalPath()
	{
		String expected = "/storage/corp/exports/Exports/General Entertainment/Compliance";
		String actual = toTest.getLocalPathToExportDestinationFolder("FOX", TranscodeJobType.COMPLIANCE_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testComplianceLocalFullPath()
	{
		String expected = "/storage/corp/exports/Exports/General Entertainment/Compliance/export.wmv";
		String actual = toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.COMPLIANCE_PROXY, "export", false);
		assertEquals(expected, actual);
	}


	@Test
	public void testComplianceLocalFullPathDVD()
	{
		String expected = "/storage/corp/exports/Exports/General Entertainment/Compliance/export";
		String actual = toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.COMPLIANCE_PROXY, "export", true);
		assertEquals(expected, actual);
	}


	@Test
	public void testCaptionFTPPath()
	{
		String expected = "captions/CaptionFTP/Unassigned";
		String actual = toTest.getFTPPathToExportDestinationFolder("FOX", TranscodeJobType.CAPTION_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testCaptionLocalPath()
	{
		String expected = "/storage/corp/captions/CaptionFTP/Unassigned";
		String actual = toTest.getLocalPathToExportDestinationFolder("FOX", TranscodeJobType.CAPTION_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testCaptionLocalFullPath()
	{
		String expected = "/storage/corp/captions/CaptionFTP/Unassigned/export.mpg";
		String actual = toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.CAPTION_PROXY, "export", false);
		assertEquals(expected, actual);
	}


	@Test
	public void testCaptionLocalFullPathDVD()
	{
		String expected = "/storage/corp/captions/CaptionFTP/Unassigned/export";
		String actual = toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.CAPTION_PROXY, "export", true);
		assertEquals(expected, actual);
	}


	@Test
	public void testPublicityFTPPathGenericChannel()
	{
		String expected = "exports/Exports/Generic/Publicity";
		String actual = toTest.getFTPPathToExportDestinationFolder("generic", TranscodeJobType.PUBLICITY_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testPublicityLocalPathGenericChannel()
	{
		String expected = "/storage/corp/exports/Exports/Generic/Publicity";
		String actual = toTest.getLocalPathToExportDestinationFolder("generic", TranscodeJobType.PUBLICITY_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testPublicityLocalFullPathGenericChannel()
	{
		String expected = "/storage/corp/exports/Exports/Generic/Publicity/export.wmv";
		String actual = toTest.getLocalPathToExportDestination("generic", TranscodeJobType.PUBLICITY_PROXY, "export", false);
		assertEquals(expected, actual);
	}


	@Test
	public void testPublicityLocalFullPathGenericChannelDVD()
	{
		String expected = "/storage/corp/exports/Exports/Generic/Publicity/export";
		String actual = toTest.getLocalPathToExportDestination("generic", TranscodeJobType.PUBLICITY_PROXY, "export", true);
		assertEquals(expected, actual);
	}


	@Test
	public void testComplianceFTPPathGenericChannel()
	{
		String expected = "exports/Exports/Generic/Compliance";
		String actual = toTest.getFTPPathToExportDestinationFolder("generic", TranscodeJobType.COMPLIANCE_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testComplianceLocalPathGenericChannel()
	{
		String expected = "/storage/corp/exports/Exports/Generic/Compliance";
		String actual = toTest.getLocalPathToExportDestinationFolder("generic", TranscodeJobType.COMPLIANCE_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testComplianceLocalFullPathGenericChannel()
	{
		String expected = "/storage/corp/exports/Exports/Generic/Compliance/export.wmv";
		String actual = toTest.getLocalPathToExportDestination("generic", TranscodeJobType.COMPLIANCE_PROXY, "export", false);
		assertEquals(expected, actual);
	}


	@Test
	public void testComplianceLocalFullPathGenericChannelDVD()
	{
		String expected = "/storage/corp/exports/Exports/Generic/Compliance/export";
		String actual = toTest.getLocalPathToExportDestination("generic", TranscodeJobType.COMPLIANCE_PROXY, "export", true);
		assertEquals(expected, actual);
	}


	@Test
	public void testCaptionFTPPathGenericChannel()
	{
		String expected = "captions/CaptionFTP/Unassigned";
		String actual = toTest.getFTPPathToExportDestinationFolder("generic", TranscodeJobType.CAPTION_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testCaptionLocalPathGenericChannel()
	{
		String expected = "/storage/corp/captions/CaptionFTP/Unassigned";
		String actual = toTest.getLocalPathToExportDestinationFolder("generic", TranscodeJobType.CAPTION_PROXY);
		assertEquals(expected, actual);
	}


	@Test
	public void testCaptionLocalFullPathGenericChannel()
	{
		String expected = "/storage/corp/captions/CaptionFTP/Unassigned/export.mpg";
		String actual = toTest.getLocalPathToExportDestination("generic", TranscodeJobType.CAPTION_PROXY, "export", false);
		assertEquals(expected, actual);
	}


	@Test
	public void testCaptionLocalFullPathGenericChannelDVD()
	{
		String expected = "/storage/corp/captions/CaptionFTP/Unassigned/export";
		String actual = toTest.getLocalPathToExportDestination("generic", TranscodeJobType.CAPTION_PROXY, "export", true);
		assertEquals(expected, actual);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testTXFTPPath()
	{
		//the functionality being tested is for exports, expect exceptions if the TX transcode job type is used
		toTest.getFTPPathToExportDestinationFolder("FOX", TranscodeJobType.TX);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testTXLocalPath()
	{
		//the functionality being tested is for exports, expect exceptions if the TX transcode job type is used
		toTest.getLocalPathToExportDestinationFolder("FOX", TranscodeJobType.TX);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testTXFullPath()
	{
		//the functionality being tested is for exports, expect exceptions if the TX transcode job type is used
		toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.TX, "export", false);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testTXFullPathDVD()
	{
		//the functionality being tested is for exports, expect exceptions if the TX transcode job type is used
		toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.TX, "export", true);
	}


	@Test
	public void testCaptionFileName()
	{
		assertEquals("ABC12345-BigBang_S02E10_v01", toTest.getFileNameForCaptionExport("ABC12345",
		                                                                               "Big Bang Theory",
		                                                                               Integer.valueOf(2),
		                                                                               Integer.valueOf(10),
		                                                                               Integer.valueOf(1)));
	}


	@Test
	public void testCaptionFileNameNonEpisodic()
	{
		assertEquals("ABC12345-BigBang_v01", toTest.getFileNameForCaptionExport("ABC12345",
		                                                                        "Big Bang Theory",
		                                                                        null,
		                                                                        null,
		                                                                        Integer.valueOf(1)));
	}


	@Test
	public void testUserDeliveryLocationCaption()
	{
		assertEquals("\\\\sydmmsvs01\\Captions\\CaptionFTP\\Unassigned\\export.mpg",
		             toTest.getUserDeliveryLocation("generic", TranscodeJobType.CAPTION_PROXY, "export", false));
	}


	@Test
	public void testUserDeliveryLocationPublicitySingleFile()
	{
		assertEquals("\\\\sydfpvs02\\MAM-Output\\Exports\\Generic\\Publicity\\export.wmv",
		             toTest.getUserDeliveryLocation("generic", TranscodeJobType.PUBLICITY_PROXY, "export", false));
	}

	@Test
	public void testUserDeliveryLocationPublicitySingleFileGrouphasSpace()
	{
		assertEquals("\\\\sydfpvs02\\MAM-Output\\Exports\\General%20Entertainment\\Publicity\\export.wmv",
		             toTest.getUserDeliveryLocation("FOX", TranscodeJobType.PUBLICITY_PROXY, "export", false));
	}

	@Test
	public void testUserDeliveryLocationPublicityDVD()
	{
		assertEquals("\\\\sydfpvs02\\MAM-Output\\Exports\\Generic\\Publicity\\export",
		             toTest.getUserDeliveryLocation("generic", TranscodeJobType.PUBLICITY_PROXY, "export", true));
	}

	@Test
	public void testUserDeliveryLocationComplianceSingleFile()
	{
		assertEquals("\\\\sydfpvs02\\MAM-Output\\Exports\\Generic\\Compliance\\export.wmv",
		             toTest.getUserDeliveryLocation("generic", TranscodeJobType.COMPLIANCE_PROXY, "export", false));
	}

	@Test
	public void testUserDeliveryLocationComplianceDVD()
	{
		assertEquals("\\\\sydfpvs02\\MAM-Output\\Exports\\Generic\\Compliance\\export",
		             toTest.getUserDeliveryLocation("generic", TranscodeJobType.COMPLIANCE_PROXY, "export", true));
	}
}
