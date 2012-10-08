package com.mediasmiths.foxtel.tc.service;

import java.io.StringReader;
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

import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.rhozet.ArrayOfPreset;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.datacontract.schemas._2004._07.rhozet.JobStatus;
import org.datacontract.schemas._2004._07.rhozet.Preset;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.carbonwfs.WfsClient;
import com.mediasmiths.foxtel.carbonwfs.WfsClientException;

public class TCRestServiceImpl implements TCRestService
{

	private static final Logger log = Logger.getLogger(TCRestServiceImpl.class);

	@Inject
	private WfsClient wfsClient;

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
			@QueryParam("preset") UUID presetID) throws WfsClientException
	{
		return wfsClient.transcode(inputPath, ouputPath, presetID, jobName);
	}

	@Override
	@Path("/job/{id}")
	@Produces("application/xml")
	public Job job(@PathParam("id") String jobid)
	{
		log.trace(String.format("jobid %s", jobid));
		return wfsClient.getJob(UUID.fromString(jobid));
	}

	@Override
	@GET
	@Path("/job/{id}/status")
	@Produces("application/xml")
	public JobStatus jobStatus(@PathParam("id") String jobid)
	{
		log.trace(String.format("jobid %s", jobid));
		return wfsClient.jobStatus(UUID.fromString(jobid));
	}

	@PUT
	@Path("/preset/create")
	@Consumes("application/xml")
	public UUID createPreset(JAXBElement<Preset> presetElement) throws JAXBException
	{
		Preset preset = presetElement.getValue();
		return wfsClient.createPreset(preset);
	}

	@Override
	@GET
	@Path("/preset")
	@Produces("application/xml")
	public ArrayOfPreset listPresets()
	{
		return wfsClient.listPresets();
	}

}
