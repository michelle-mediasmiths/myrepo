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
				URI.create("http://10.111.224.101:8080/fx-TcAdapter"));

			String[] jobs = new String[]{"463f5ffe-83dd-4f59-ba7b-c032b732c4fc",
					"2010f439-0fc2-434f-852d-8a28fd563aa1",
					"abfbd47e-2de0-4497-b611-69ba3e1192ac",
					"d3ce7733-2645-4c15-80e0-fd9a9fe81135",
					"6122b907-d9cc-46f0-9649-ec362d13b07e",
					"7c353b63-e8d0-4695-a851-db46bf80c9a1",
					"4748a86c-758f-4d04-84a2-d79153edf42d",
					"5749a992-ce2e-45de-9bbd-b80192783d8b",
					"61289354-5ee9-4460-8196-dd5fcdb52465",
					"70911230-7c5f-4b5e-b0b8-6815d127a785",
					"56ed68ad-0deb-41a1-8ded-8ecc55181c4f",
					"8ac86153-8638-4439-9750-f196cd32d526",
					"c4328ae7-3b7c-43eb-9145-bac4498e8fe5",
					"ce9ebb5a-574f-4915-b689-332d23474bb9",
					"1eaf2e98-767e-4cb0-b177-dbd7db96090d",
					"be00dace-1029-40db-9fb8-f59f1a2216fc"};
	
	
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
