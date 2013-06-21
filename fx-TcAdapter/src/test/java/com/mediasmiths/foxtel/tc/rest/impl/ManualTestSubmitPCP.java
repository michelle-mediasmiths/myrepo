package com.mediasmiths.foxtel.tc.rest.impl;

import com.google.inject.Injector;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCFTPUpload;
import com.mediasmiths.foxtel.tc.rest.api.TCJobInfo;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCResolution;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.std.guice.apploader.BasicSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.std.guice.web.rest.CoreRestServicesModule;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

import java.net.URI;

public class ManualTestSubmitPCP
{
	public static void main(String[] args) throws Exception
	{
		Injector injector = GuiceInjectorBootstrap.createInjector(new PropertyFile(),
		                                                          new BasicSetup(new CoreRestServicesModule()));

		String endpoint = "http://127.0.0.1:8080/fx-TcAdapter"; // http://10.111.224.101:8080/fx-TcAdapter
		TCRestService svc = injector.getInstance(JAXRSProxyClientFactory.class).createClient(TCRestService.class,
		                                                                                     URI.create(endpoint));

		TCJobParameters params = new TCJobParameters();

		///storage/mam/hires01/hr/hr01/2013/01/26/BM-250113-M2_0125743.mxf

//		params.inputFile = "/storage/mam/hires01/mediasmiths/input/in.mxf";
//		params.outputFolder = "/storage/mam/hires01/mediasmiths/output/ARN_TR";
		
		params.inputFile = "/Volumes/storage-readonly/foxtel/samples/MAM HD Test1.mxf";
		params.outputFolder = "/Volumes/foxtel/tcoutput";		
		params.outputFileBasename ="test_dvd_output_then_failed_upload";
		params.description =  "test dvd output with failed upload";
		
//		params.inputFile = "/storage/mam/hires01/tcinput/HD1.mxf";
//		params.outputFolder = "/storage/mam/hires01/tcoutput";

//		params.bug=new TCBugOptions();
//		params.bug.channel = "ARN";
//		params.bug.position = TCLocation.TOP_RIGHT;
		
		params.timecode = null;
		
		params.audioType = TCAudioType.DOLBY_E;
		params.purpose = TCOutputPurpose.DVD;

		params.resolution = TCResolution.HD;
		params.ftpupload = new TCFTPUpload();
		
		params.ftpupload.filename = "output_dvd_with_failed_upload";
		params.ftpupload.folder = "CaptionFTP/Unassigned";
		params.ftpupload.user = "wronguser";
		params.ftpupload.password="wrongpassword";
		params.ftpupload.server = "192.168.2.68";
		
		// Display the generated Project
		String xml = svc.createPCPXML(params);
		System.out.println("XML generated: " + xml);

		// Submit the job to WFS
		String id = svc.createJob(params);
		System.out.println("Job created: " + id);

		// Query the job details and then serialise them
		TCJobInfo job = svc.queryJob(id);
		System.out.println(JAXBSerialiser.getInstance(TCJobInfo.class).serialise(job));
	}
}
