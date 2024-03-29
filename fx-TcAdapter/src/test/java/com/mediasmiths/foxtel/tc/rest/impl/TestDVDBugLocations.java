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

public class TestDVDBugLocations
{
	public static void main(String[] args) throws Exception
	{
		Injector injector = GuiceInjectorBootstrap.createInjector(
				new PropertyFile(),
				new BasicSetup(new CoreRestServicesModule()));

		TCRestService svc = injector.getInstance(JAXRSProxyClientFactory.class).createClient(
				TCRestService.class,
				URI.create("http://10.111.224.101:8080/fx-TcAdapter"));

		String inputFile = "/storage/mam/hires01/mediasmiths/input/in.mxf";
		String outputFolder = "/storage/mam/hires01/mediasmiths/output/";
		String[] channels = new String[] { "ARN", "BIO", "HIT", "CIN", "FOX", "HST", "LST", "LSY", "FBO", "MEV", "FKC", "FOD",
				"AED", "SOH" };
		String[] testNumbers = new String[] { "1.14.18", "1.14.19", "1.14.20", "1.14.21", "1.14.22", "1.14.23", "1.14.24",
				"1.14.25", "1.14.26", "1.14.27", "1.14.28", "1.14.29", "1.14.30", "1.14.28-SOHO" };
		TCLocation[] bugLocations = new TCLocation[] { TCLocation.TOP_RIGHT, TCLocation.BOTTOM_RIGHT, TCLocation.BOTTOM_LEFT,
				TCLocation.TOP_LEFT, TCLocation.TOP_LEFT, TCLocation.TOP_LEFT, TCLocation.TOP_LEFT, TCLocation.TOP_LEFT,
				TCLocation.TOP_LEFT, TCLocation.TOP_LEFT, TCLocation.TOP_LEFT, TCLocation.TOP_LEFT, TCLocation.TOP_LEFT, TCLocation.TOP_LEFT };

		if (!(channels.length == testNumbers.length && testNumbers.length == bugLocations.length))
		{
			System.out.println("channels "+channels.length+ " testnumbers" + testNumbers.length + " buglocations " + bugLocations.length);
			System.out.println("arrays sizes dont match, check input");
			System.exit(1);
		}

		for (int i = 0; i < testNumbers.length; i++)
		{

			String channel = channels[i];
			String testNumber = testNumbers[i];
			TCLocation bugLocation = bugLocations[i];

			System.out.println("***********************************");
			System.out.println("Test " + testNumber);
			System.out.println("***********************************");

			try
			{

				TCJobParameters params = new TCJobParameters();

				params.inputFile = inputFile;
				params.outputFolder = outputFolder;
				params.outputFileBasename = channel + "_" + bugLocation.toString();

				params.bug.channel = channel;
				params.bug.position = bugLocation;
				params.timecode = null;
				params.audioType = TCAudioType.DOLBY_E;
				params.purpose = TCOutputPurpose.DVD;

				// Display the generated Project
				String xml = svc.createPCPXML(params);
				System.out.println(testNumber + " XML generated: " + xml);

				// Submit the job to WFS
				String id = svc.createJob(params);
				System.out.println(testNumber + " Job created: " + id);
			}
			catch (Exception e)
			{
				System.out.println("Test " + testNumber + " error " + e.getMessage());
				e.printStackTrace();
			}

		}

	}
}
