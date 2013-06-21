package com.mediasmiths.foxtel.tc.rest.impl;

import com.google.inject.Injector;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCBugOptions;
import com.mediasmiths.foxtel.tc.rest.api.TCJobInfo;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCLocation;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.foxtel.tc.rest.api.TCTimecodeColour;
import com.mediasmiths.foxtel.tc.rest.api.TCTimecodeOptions;
import com.mediasmiths.foxtel.tc.rest.api.TCTimecodeSize;
import com.mediasmiths.std.guice.apploader.BasicSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.std.guice.web.rest.CoreRestServicesModule;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

import java.net.URI;

public class TestTimeCodeOptions
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
	
		TCLocation[] locations = new TCLocation[] {TCLocation.TOP, TCLocation.BOTTOM,TCLocation.TOP, TCLocation.BOTTOM};
		TCTimecodeColour[][] colors = new TCTimecodeColour[2][];
		
		colors[0] = new TCTimecodeColour[]{TCTimecodeColour.BLACK,TCTimecodeColour.BLACK, TCTimecodeColour.WHITE, TCTimecodeColour.WHITE};
		colors[1] = new TCTimecodeColour[]{TCTimecodeColour.WHITE,TCTimecodeColour.WHITE, TCTimecodeColour.BLACK, TCTimecodeColour.BLACK};
		
		

		for (int i = 0; i < locations.length; i++)
		{

			String channel ="BIO";
			TCLocation buglocation = TCLocation.BOTTOM_RIGHT;
			TCTimecodeColour timecodeForeGround = colors[0][i];
			TCTimecodeColour timecodeBackGround = colors[1][i];
			TCLocation timecodeLocation = locations[i];
			String name = channel + "_TC_" + timecodeForeGround.toString()+"_ON_"+timecodeBackGround.toString()+"_"+timecodeLocation.toString();
			
			System.out.println("******************************");
			System.out.println(name);
			System.out.println("******************************");
			
			try
			{

				TCJobParameters params = new TCJobParameters();

				TCTimecodeOptions tcop = new TCTimecodeOptions();
				tcop.background=timecodeBackGround;
				tcop.foreground=timecodeForeGround;
				tcop.location=timecodeLocation;
				tcop.size=TCTimecodeSize.LARGE;
				
				
				params.inputFile = inputFile;
				params.outputFolder = outputFolder;
				params.outputFileBasename = channel + "_" + buglocation.toString()+ "_TC_" + timecodeForeGround.toString()+"_ON_"+timecodeBackGround.toString()+"_"+timecodeLocation.toString();

				params.bug=new TCBugOptions();
				params.bug.channel = channel;
				params.bug.position = buglocation;
				params.timecode =tcop; 
				params.audioType = TCAudioType.DOLBY_E;
				params.purpose = TCOutputPurpose.DVD;

				// Display the generated Project
				String xml = svc.createPCPXML(params);
				System.out.println(name + " XML generated: " + xml);

				// Submit the job to WFS
				String id = svc.createJob(params);
				System.out.println(name + " Job created: " + id);
			}
			catch (Exception e)
			{
				System.out.println("Test " + name + " error " + e.getMessage());
				e.printStackTrace();
			}

		}

	}
}
