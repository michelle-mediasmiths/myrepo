package com.mediasmiths.foxtel.tc.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.datacontract.schemas._2004._07.rhozet.JobStatus;
import org.datacontract.schemas._2004._07.rhozet.Preset;
import org.jboss.resteasy.spi.InjectorFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mediasmiths.foxtel.carbonwfs.Client;
import com.mediasmiths.foxtel.carbonwfs.WfsClientException;

public class TCRestServiceImpl implements TCRestService
{

	@Inject private Client wfsClient;
	
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
	public UUID transcode(
			@QueryParam("jobname") String jobName,
			@QueryParam("input") String inputPath,
			@QueryParam("output") String ouputPath,
			@QueryParam("profile") UUID profileID) throws WfsClientException
	{
		return wfsClient.transcode(inputPath, ouputPath, profileID, jobName);
	}

	@Override
	@GET
	@Path("/job/{id}/status")
	@Produces("application/xml")
	public JobStatus jobStatus(@PathParam("id") UUID jobid)
	{
		return wfsClient.jobStatus(jobid);
	}

	@Override
	@PUT
	@Path("/profile/create")
	@Consumes("application/x-www-form-urlencoded")
	public UUID createProfile(@FormParam("profile") String profileXML)
	{
		return wfsClient.createProfile(profileXML);
	}
	


}
