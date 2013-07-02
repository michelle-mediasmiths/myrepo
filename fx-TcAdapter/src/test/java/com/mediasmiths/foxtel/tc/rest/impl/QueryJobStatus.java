package com.mediasmiths.foxtel.tc.rest.impl;

import com.google.inject.Injector;
import com.mediasmiths.foxtel.tc.rest.api.TCJobInfo;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.std.guice.apploader.BasicSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.std.guice.web.rest.CoreRestServicesModule;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

import java.net.URI;

public class QueryJobStatus
{
	public static void main(String[] args) throws Exception
	{
		Injector injector = GuiceInjectorBootstrap.createInjector(
				new PropertyFile(),
				new BasicSetup(new CoreRestServicesModule()));

		TCRestService svc = injector.getInstance(JAXRSProxyClientFactory.class).createClient(
				TCRestService.class,
				URI.create("http://127.0.0.1:8080/fx-TcAdapter"));

		String[] jobs = new String[]{"42b53bc2-58e8-4679-a1b6-96a0683bdb46",
		                             "ff814946-705d-4dd7-9f27-16d5bf40f848",
		                             "33e5db53-e2cc-4d45-868a-8d4ad4bb6ed1",
		                             "448645fe-ee51-4087-acfb-c55752b3e324",
		                             "bc88fed0-0183-4ae6-b36c-effc89834f05",
		                             "a7d778fb-96e9-456d-93af-3b19df32ce51",
		                             "87a64459-6e3a-433d-b72a-7083389c309b",
		                             "1daf4fe8-403e-4350-a577-614776dc71cb",
		                             "1bf63f96-c96d-4353-a52b-754005db221d",
		                             "ab6d18aa-9e87-4670-a6d2-22e196d1b98a"};
	
	
		for (int i = 0; i < jobs.length; i++)
		{
			
			String jobid = jobs[i];
			System.out.println(jobid);
			// Query the job details and then serialise them
			TCJobInfo job = svc.queryJob(jobid);
			System.out.println(JAXBSerialiser.getInstance(TCJobInfo.class).serialise(job));

		}

	}
}
