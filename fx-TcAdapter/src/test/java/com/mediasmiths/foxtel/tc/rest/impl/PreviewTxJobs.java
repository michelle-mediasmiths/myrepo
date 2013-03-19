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

public class PreviewTxJobs
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
				URI.create("http://localhost:8080/fx-TcAdapter"));

		/////// TX HD ////////
		String name = "HD Input TX HD Output (stereo)";
		String job = generateJob(name, TCOutputPurpose.TX_HD, TCAudioType.STEREO, TCResolution.HD);
		printJob(name,job);
		
		name = "HD Input TX HD Output (dolby)";
		generateJob(name, TCOutputPurpose.TX_HD, TCAudioType.DOLBY_E, TCResolution.HD);
		printJob(name,job);
		
		/////// TX SD ////////
		
		name = "HD Input TX SD Output (stereo)";
		job = generateJob(name, TCOutputPurpose.TX_SD, TCAudioType.STEREO, TCResolution.HD);
		printJob(name,job);
		
		name = "HD Input TX SD Output (dolby)";
		generateJob(name, TCOutputPurpose.TX_SD, TCAudioType.DOLBY_E, TCResolution.HD);
		printJob(name,job);
		
		name = "SD Input TX SD Output (stereo)";
		job = generateJob(name, TCOutputPurpose.TX_SD, TCAudioType.STEREO, TCResolution.SD);
		printJob(name,job);
		
		name = "SD Input TX SD Output (dolby)";
		job = generateJob(name, TCOutputPurpose.TX_SD, TCAudioType.DOLBY_E, TCResolution.SD);
		printJob(name,job);
		
		
	}
	private static void printJob(String name, String job)
	{
		System.out.println("************************");
		System.out.println(name);
		System.out.println("************************");
		System.out.println(job);
		System.out.println("************************");
	}
	private static String generateJob(String name,TCOutputPurpose purpose, TCAudioType audio, TCResolution resolution) throws Exception{
		
		TCJobParameters jobParams = new TCJobParameters();
		jobParams.purpose = purpose;
		jobParams.audioType = audio;
		jobParams.resolution = resolution;

		jobParams.inputFile = inputFileLocation;
		jobParams.outputFolder = transcodeBaseOutputLocation;
		jobParams.outputFileBasename = name;

		jobParams.priority = 1;

		return svc.createPCPXML(jobParams);
	}


}
