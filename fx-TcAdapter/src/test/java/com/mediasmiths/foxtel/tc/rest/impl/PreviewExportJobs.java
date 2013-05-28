package com.mediasmiths.foxtel.tc.rest.impl;

import java.net.URI;
import java.sql.Time;

import org.apache.cxf.binding.soap.tcp.TCPConduit;
import org.junit.Test;

import com.google.inject.Injector;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCBugOptions;
import com.mediasmiths.foxtel.tc.rest.api.TCFTPUpload;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCLocation;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCResolution;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.foxtel.tc.rest.api.TCTimecodeColour;
import com.mediasmiths.foxtel.tc.rest.api.TCTimecodeOptions;
import com.mediasmiths.foxtel.tc.rest.api.TCTimecodeSize;
import com.mediasmiths.std.guice.apploader.BasicSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.std.guice.web.rest.CoreRestServicesModule;
import com.mediasmiths.std.io.PropertyFile;

public class PreviewExportJobs
{

	static String inputFileLocation = "/Volumes/foxtel/tcinput/input.mxf";
	static String transcodeBaseOutputLocation = "/Volumes/foxtel/tcoutput/";
	
	static TCRestService svc;
	
	public static void main(String[] args) throws Exception
	{
		Injector injector = GuiceInjectorBootstrap.createInjector(
				new PropertyFile(),
				new BasicSetup(new CoreRestServicesModule()));

		svc = injector.getInstance(JAXRSProxyClientFactory.class).createClient(
				TCRestService.class,
				URI.create("http://192.168.2.22:8080/fx-TcAdapter"));
	
		String complianceJob = generateComplianceJob(TCOutputPurpose.MPG4);
		System.out.println("***************************");
		System.out.println("Compliance mpg");
		System.out.println("***************************");
		System.out.println(complianceJob);
		System.out.println("***************************");

		complianceJob = generateComplianceJob(TCOutputPurpose.DVD);
			System.out.println("***************************");
			System.out.println("Compliance dvd");
			System.out.println("***************************");
			System.out.println(complianceJob);
			System.out.println("***************************");

		
		String captionJob = generateCaptionJob();
		System.out.println("***************************");
		System.out.println("Caption");
		System.out.println("***************************");
		System.out.println(captionJob);
		System.out.println("***************************");

		String publicityJob = generatePublicityJob(TCOutputPurpose.MPG4);
		System.out.println("***************************");
		System.out.println("Publicity mpg");
		System.out.println("***************************");
		System.out.println(publicityJob);
		System.out.println("***************************");
		
		publicityJob = generatePublicityJob(TCOutputPurpose.DVD);
		System.out.println("***************************");
		System.out.println("Publicity dvd");
		System.out.println("***************************");
		System.out.println(publicityJob);
		System.out.println("***************************");
		
	}


	private static String generatePublicityJob(TCOutputPurpose p) throws Exception
	{
		TCJobParameters jobParams = new TCJobParameters();
		jobParams.purpose = p;
		jobParams.audioType = TCAudioType.STEREO;
		jobParams.resolution = TCResolution.SD;

		jobParams.inputFile = inputFileLocation;
		jobParams.outputFolder = transcodeBaseOutputLocation + "publicity/";
		jobParams.outputFileBasename = "PublicityOutputTest";

		jobParams.bug = new TCBugOptions();
		jobParams.bug.channel = "LST";
		jobParams.bug.position = TCLocation.TOP_LEFT;
		jobParams.bug.opacity = 80.0d;

		jobParams.timecode = new TCTimecodeOptions();
		jobParams.timecode.background = TCTimecodeColour.WHITE;
		jobParams.timecode.foreground = TCTimecodeColour.BLACK;
		jobParams.timecode.location = TCLocation.BOTTOM;
		jobParams.timecode.size = TCTimecodeSize.LARGE;

		jobParams.priority = 1;

		jobParams.ftpupload = getBaseFtpUploadParameters();
		if(p==TCOutputPurpose.MPG4){
			jobParams.ftpupload.filename =  jobParams.outputFileBasename + ".mpg";
		}
			
		jobParams.ftpupload.folder = "exports/Exports/Lifestyle/Publicity";

		return svc.createPCPXML(jobParams);
//		return svc.createJob(jobParams);
	}

	private static String generateCaptionJob() throws Exception
	{
		TCJobParameters jobParams = new TCJobParameters();
		jobParams.purpose = TCOutputPurpose.CAPTIONING;
		jobParams.audioType = TCAudioType.STEREO;
		jobParams.resolution = TCResolution.SD;

		jobParams.inputFile = inputFileLocation;
		jobParams.outputFolder = transcodeBaseOutputLocation + "caption/";
		jobParams.outputFileBasename = "CaptionOutputTest";

		jobParams.timecode = new TCTimecodeOptions();
		jobParams.timecode.background = TCTimecodeColour.WHITE;
		jobParams.timecode.foreground = TCTimecodeColour.BLACK;
		jobParams.timecode.location = TCLocation.BOTTOM;
		jobParams.timecode.size = TCTimecodeSize.LARGE;

		jobParams.priority = 1;

		jobParams.ftpupload = getBaseFtpUploadParameters();
		jobParams.ftpupload.filename = jobParams.outputFileBasename + ".mpg";
		jobParams.ftpupload.folder = "captions/CaptionFTP/Unassigned";

		return svc.createPCPXML(jobParams);

	}

	private static String generateComplianceJob(TCOutputPurpose p) throws Exception
	{
		TCJobParameters jobParams = new TCJobParameters();
		jobParams.purpose = p;
		jobParams.audioType = TCAudioType.STEREO;
		jobParams.resolution = TCResolution.SD;

		jobParams.inputFile = inputFileLocation;
		jobParams.outputFolder = transcodeBaseOutputLocation + "compliance/";
		jobParams.outputFileBasename = "ComplianceOutputTest";

		jobParams.bug = new TCBugOptions();
		jobParams.bug.channel = "LST";
		jobParams.bug.position = TCLocation.TOP_LEFT;
		jobParams.bug.opacity = 80.0d;
		
		jobParams.timecode = new TCTimecodeOptions();
		jobParams.timecode.background = TCTimecodeColour.WHITE;
		jobParams.timecode.foreground = TCTimecodeColour.BLACK;
		jobParams.timecode.location = TCLocation.BOTTOM;
		jobParams.timecode.size = TCTimecodeSize.LARGE;

		jobParams.priority = 1;

		jobParams.ftpupload = getBaseFtpUploadParameters();
		
		if(p==TCOutputPurpose.MPG4){
		jobParams.ftpupload.filename =  jobParams.outputFileBasename + ".mpg";
		}
		
		jobParams.ftpupload.folder = "exports/Exports/Lifestyle/Compliance";

		return svc.createPCPXML(jobParams);

	}

	private static TCFTPUpload getBaseFtpUploadParameters()
	{
		TCFTPUpload ftpupload = new TCFTPUpload();

//		ftpupload.server = "10.111.224.151";
//		ftpupload.user = "mamexportftp";
//		ftpupload.password = "ftp3xp0rt";
		ftpupload.server = "192.168.2.30";
		ftpupload.user = "targetuser";
		ftpupload.password = "password";
		

		return ftpupload;

	}

}
