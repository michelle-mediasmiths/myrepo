package com.mediasmiths.foxtel.tc.rest.impl;

import com.google.inject.Injector;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.std.guice.apploader.BasicSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.std.guice.web.rest.CoreRestServicesModule;
import com.mediasmiths.std.io.PropertyFile;

import java.net.URI;

public class TestBuildPCP
{
	public static void main(String[] args) throws Exception
	{
		Injector injector = GuiceInjectorBootstrap.createInjector(new PropertyFile(),
		                                                          new BasicSetup(new CoreRestServicesModule()));

		TCRestService svc = injector.getInstance(JAXRSProxyClientFactory.class).createClient(TCRestService.class,
		                                                                                     URI.create("http://localhost:8080/tcadapter"));

		TCJobParameters params = new TCJobParameters();
		params.inputFile = "/Volumes/storage-readonly/foxtel/video.mpg";
		params.outputFolder = "/Users/pwright/";
		params.bug.channel = "AED";
		params.audioType = TCAudioType.DOLBY_E;
		params.purpose = TCOutputPurpose.DVD;

		String xml = svc.createPCPXML(params);

		System.out.println("XML generated: " + xml);

		String id = svc.createJob(params);

		System.out.println("Job created: " + id);
	}
}
