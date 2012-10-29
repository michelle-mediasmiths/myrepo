package com.mediasmiths.foxtel.tc.service;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.rhozet.ArrayOfPreset;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.datacontract.schemas._2004._07.rhozet.JobStatus;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.carbonwfs.WfsClient;
import com.mediasmiths.foxtel.carbonwfs.WfsClientException;
import com.mediasmiths.foxtel.tc.JobBuilder;
import com.mediasmiths.foxtel.tc.JobBuilderException;
import com.mediasmiths.foxtel.tc.model.TCBuildJobXMLRequest;
import com.mediasmiths.foxtel.tc.model.TCStartRequest;
import com.mediasmiths.mayam.MayamClientException;

public class TCRestServiceImpl implements TCRestService
{

	private static final Logger log = Logger.getLogger(TCRestServiceImpl.class);

	@Inject
	private WfsClient wfsClient;
	@Inject
	private JobBuilder jobBuilder;

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
	@Path("/job/start/{TCstartRequest}")
	@Produces("text/plain")
	public UUID transcode(@PathParam("TCstartRequest") TCStartRequest startRequest) throws WfsClientException
	{
		String jobXml = startRequest.getPcpXml();
		log.trace("trying to create job from xml " + jobXml);
		UUID jobid =  wfsClient.transcode(jobXml);
		
		//dont yet know how to set the priority as part of the jobXML
		wfsClient.updatejobPriority(jobid, startRequest.getPriority());
		
		
		
		return jobid;
		
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

	@Override
	@GET
	@Path("/job/{id}/finished")
	@Produces("text/plain")
	public Boolean jobFinished(@PathParam("id") String jobid)
	{
		JobStatus status = jobStatus(jobid);

		return status == JobStatus.COMPLETED || status == JobStatus.COMPLETED || status == JobStatus.FATAL;
	}

	@Override
	@GET
	@Path("/preset")
	@Produces("application/xml")
	public ArrayOfPreset listPresets()
	{
		return wfsClient.listPresets();
	}

	@Override
	@POST
	@Path("/job/build/")
	@Produces("application/xml")
	public String buildJobXMLForTranscode(TCBuildJobXMLRequest buildJobXMLRequest) throws MayamClientException, JobBuilderException
	{
		String job = jobBuilder.buildJobForTxPackageTranscode(buildJobXMLRequest.getPackageID(), buildJobXMLRequest.getInputFile(), buildJobXMLRequest.getOutputFolder());
		return job;
	}
	
	@Override
	@GET
	@Path("/job/{id}/success")
	@Produces("text/plain")
	public Boolean jobSuccessful(@PathParam("id") String jobid)
	{
		JobStatus status = jobStatus(jobid);
		return status == JobStatus.COMPLETED;
	}

	@Override
	@POST
	@Path("/jobs/")
	@Produces("application/xml")
	public List<Job> listJobs() {
		log.debug("listing jobs");		
		return wfsClient.listJobs();	
	}
	
}
