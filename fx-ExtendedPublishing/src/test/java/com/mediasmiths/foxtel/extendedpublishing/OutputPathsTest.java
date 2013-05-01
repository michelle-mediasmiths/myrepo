package com.mediasmiths.foxtel.extendedpublishing;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.name.Names;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.std.io.PropertyFile;

@RunWith(JukitoRunner.class)
public class OutputPathsTest
{
	protected final static Logger log = Logger.getLogger(OutputPathsTest.class);

	public static class Module extends JukitoModule
	{
		protected void configureTest()
		{
			PropertyFile properties = PropertyFile.find("environment.properties");
			Names.bindProperties(this.binder(), properties.toProperties());
			bind(ChannelProperties.class).to(ChannelPropertiesStub.class);
		}
	}

	@Inject
	OutputPaths toTest;

	@Test
	public void testPublicityFTPPath()
	{
		String expected = "exports/Exports/General Entertainment/Publicity";
		String actual = toTest.getFTPPathToExportDestinationFolder("FOX", TranscodeJobType.PUBLICITY_PROXY);
		assertEquals(expected,actual);
	}
	
	@Test
	public void testPublicityLocalPath()
	{
		String expected = "/storage/corp/exports/Exports/General Entertainment/Publicity";
		String actual = toTest.getLocalPathToExportDestinationFolder("FOX", TranscodeJobType.PUBLICITY_PROXY);
		assertEquals(expected,actual);
	}
	
	@Test
	public void testPublicityLocalFullPath(){
		String expected = "/storage/corp/exports/Exports/General Entertainment/Publicity/export.wmv";
		String actual = toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.PUBLICITY_PROXY, "export");
		assertEquals(expected,actual);
	}
	

	@Test
	public void testComplianceFTPPath()
	{
		String expected = "exports/Exports/General Entertainment/Compliance";
		String actual = toTest.getFTPPathToExportDestinationFolder("FOX", TranscodeJobType.COMPLIANCE_PROXY);
		assertEquals(expected,actual);
	}
	
	@Test
	public void testComplianceLocalPath()
	{
		String expected = "/storage/corp/exports/Exports/General Entertainment/Compliance";
		String actual = toTest.getLocalPathToExportDestinationFolder("FOX", TranscodeJobType.COMPLIANCE_PROXY);
		assertEquals(expected,actual);
	}
	
	@Test
	public void testComplianceLocalFullPath(){
		String expected = "/storage/corp/exports/Exports/General Entertainment/Compliance/export.wmv";
		String actual = toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.COMPLIANCE_PROXY, "export");
		assertEquals(expected,actual);
	}
	
	
	@Test
	public void testCaptionFTPPath()
	{
		String expected = "captions/CaptionFTP/Unassigned";
		String actual = toTest.getFTPPathToExportDestinationFolder("FOX", TranscodeJobType.CAPTION_PROXY);
		assertEquals(expected,actual);
	}
	
	@Test
	public void testCaptionLocalPath()
	{
		String expected = "/storage/corp/captions/CaptionFTP/Unassigned";
		String actual = toTest.getLocalPathToExportDestinationFolder("FOX", TranscodeJobType.CAPTION_PROXY);
		assertEquals(expected,actual);
	}
	
	@Test
	public void testCaptionLocalFullPath(){
		String expected = "/storage/corp/captions/CaptionFTP/Unassigned/export.mpg";
		String actual = toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.CAPTION_PROXY, "export");
		assertEquals(expected,actual);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testTXFTPPath(){
		//the functionality being tested is for exports, expect exceptions if the TX transcode job type is used
		toTest.getFTPPathToExportDestinationFolder("FOX", TranscodeJobType.TX);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testTXLocalPath(){
		//the functionality being tested is for exports, expect exceptions if the TX transcode job type is used
		toTest.getLocalPathToExportDestinationFolder("FOX", TranscodeJobType.TX);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testTXFullPath(){
		//the functionality being tested is for exports, expect exceptions if the TX transcode job type is used
		toTest.getLocalPathToExportDestination("FOX", TranscodeJobType.TX, "export");
	}
	
	
}
