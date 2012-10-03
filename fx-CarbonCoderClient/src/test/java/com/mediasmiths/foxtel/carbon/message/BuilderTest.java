package com.mediasmiths.foxtel.carbon.message;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.junit.Test;

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

}
