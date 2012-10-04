package com.mediasmiths.foxtel.carbon.message;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.carbon.CarbonClient;
import com.mediasmiths.foxtel.carbon.guice.CarbonClientModule;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;

public class BuilderTest
{
	private static final Logger log = Logger.getLogger(BuilderTest.class);

	@Test
	public void testBuildJobQueueOneInOneOut() throws ParserConfigurationException, TransformerException
	{

		Builder b = new Builder();

		String jobQueueRequest = b.getJobQueueRequest(
				"myjob",
				Arrays.asList(new String[] { "input.mxf" }),
				Arrays.asList(new String[] { "output.mxf" }),
				Arrays.asList(new UUID[] { UUID.fromString("cb15d0f4-e61b-42ec-acc2-853f6fb442af") }));

		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<cnpsXML CarbonAPIVer=\"1.2\" JobName=\"myjob\" TaskType=\"JobQueue\">\n<Sources>\n<Module_0 Filename=\"input.mxf\"/>\n</Sources>\n<Destinations>\n<Module_0 DestinationName=\"output.mxf\" PresetGUID=\"cb15d0f4-e61b-42ec-acc2-853f6fb442af\"/>\n</Destinations>\n</cnpsXML>\n",
				jobQueueRequest);

		log.info(jobQueueRequest);
	}
	
//	@Test
//	public void listjobs() throws UnknownHostException, TransformerException, ParserConfigurationException, IOException, JAXBException{
//
//		final Injector injector = GuiceInjectorBootstrap.createInjector(new GuiceSetup()
//		{
//			
//			@Override
//			public void registerModules(List<Module> modules, PropertyFile config)
//			{
//				modules.add(new CarbonClientModule());
//				
//			}
//			
//			@Override
//			public void injectorCreated(Injector injector)
//			{
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		
//		
//		CarbonClient carbonclient = injector.getInstance(CarbonClient.class);
//		
//		carbonclient.listJobs();
//	}
//	
//	@Test
//	public void listprofiles() throws UnknownHostException, TransformerException, ParserConfigurationException, IOException, JAXBException{
//
//		final Injector injector = GuiceInjectorBootstrap.createInjector(new GuiceSetup()
//		{
//			
//			@Override
//			public void registerModules(List<Module> modules, PropertyFile config)
//			{
//				modules.add(new CarbonClientModule());
//				
//			}
//			
//			@Override
//			public void injectorCreated(Injector injector)
//			{
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		
//		
//		CarbonClient carbonclient = injector.getInstance(CarbonClient.class);
//		
//		carbonclient.listProfiles();
//	}

}
