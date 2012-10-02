package com.mediasmiths.foxtel.carbon.message;

import java.util.Arrays;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Test;

public class BuilderTest
{

	@Test
	public void testBuildJobQueueOneInOneOut() throws ParserConfigurationException, TransformerException
	{

		Builder b = new Builder();

		String jobQueueRequest = b.getJobQueueRequest(
				"myjob",
				Arrays.asList(new String[] { "input.mxf" }),
				Arrays.asList(new String[] { "output.mxf" }),
				Arrays.asList(new UUID[] { UUID.fromString("cb15d0f4-e61b-42ec-acc2-853f6fb442af") }));

		System.out.println(jobQueueRequest);
	}

}
