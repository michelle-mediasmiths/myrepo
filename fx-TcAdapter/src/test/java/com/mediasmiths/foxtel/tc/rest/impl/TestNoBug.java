package com.mediasmiths.foxtel.tc.rest.impl;

import com.google.inject.Injector;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCJobInfo;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCLocation;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.std.guice.apploader.BasicSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.std.guice.web.rest.CoreRestServicesModule;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

import java.net.URI;

public class TestNoBug
{
	public static void main(String[] args) throws Exception
	{
		Injector injector = GuiceInjectorBootstrap.createInjector(new PropertyFile(),
		                                                          new BasicSetup(new CoreRestServicesModule()));

		TCRestService svc = injector.getInstance(JAXRSProxyClientFactory.class).createClient(TCRestService.class,
		                                                                                     URI.create("http://10.111.224.101:8080/fx-TcAdapter"));

		TCJobParameters params = new TCJobParameters();
		
		
		params.inputFile = "/storage/mam/hires01/mediasmiths/input/in.mxf";
		params.outputFolder = "/storage/mam/hires01/mediasmiths/output/";
		params.outputFileBasename="NO_BUG_NO_TC_2";
		params.audioType = TCAudioType.DOLBY_E;
		params.purpose = TCOutputPurpose.DVD;
		params.bug=null;
		params.timecode=null;
		params.priority=6;


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
