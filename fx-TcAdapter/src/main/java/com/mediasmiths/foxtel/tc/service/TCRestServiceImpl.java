package com.mediasmiths.foxtel.tc.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jboss.resteasy.spi.InjectorFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mediasmiths.foxtel.carbon.CarbonClient;

public class TCRestServiceImpl implements TCRestService
{

	private final CarbonClient carbonClient;

	@Inject
	public TCRestServiceImpl(CarbonClient carbonClient)
	{
		this.carbonClient = carbonClient;
	}

	@Override
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping()
	{
		return "ping";
	}

	@Override
	@PUT
	@Path("/start")
	public String transcode(
			@QueryParam("jobname") String jobName,
			@QueryParam("input") String inputPath,
			@QueryParam("output") String ouputPath,
			@QueryParam("profile") String profileName) throws UnknownHostException, TransformerException, ParserConfigurationException, IOException
	{
		return carbonClient.jobQueueRequest(
				jobName,
				Arrays.asList(new String[] { inputPath }),
				Arrays.asList(new String[] { ouputPath }),
				Arrays.asList(new UUID[] { UUID.randomUUID() }));
	}

	@Override
	@GET
	@Path("/profiles")
	@Produces("text/plain")
	public List<String> listProfiles() throws TransformerException, ParserConfigurationException, UnknownHostException, IOException
	{
		return carbonClient.listProfiles();
	}

}
