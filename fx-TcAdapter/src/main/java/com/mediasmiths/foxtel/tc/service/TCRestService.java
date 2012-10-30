package com.mediasmiths.foxtel.tc.service;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.datacontract.schemas._2004._07.rhozet.ArrayOfPreset;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.datacontract.schemas._2004._07.rhozet.JobStatus;

import com.mediasmiths.foxtel.carbonwfs.WfsClientException;
import com.mediasmiths.foxtel.tc.JobBuilderException;
import com.mediasmiths.foxtel.tc.model.TCBuildJobXMLRequest;
import com.mediasmiths.foxtel.tc.model.TCStartRequest;
import com.mediasmiths.mayam.MayamClientException;


@Path("/tc")
public interface TCRestService
{
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();

	
	@POST
	@Path("/job/build/")
	@Produces("text/plain")
	public String buildJobXMLForTranscode(TCBuildJobXMLRequest buildJobXMLRequest) throws MayamClientException, JobBuilderException;
	
	@PUT
	@Path("/job/start/")
	@Produces("text/plain")
	public UUID transcode(TCStartRequest startRequest) throws WfsClientException;
	

	@GET
	@Path("/job/{id}/status")
	@Produces("application/xml")
	public JobStatus jobStatus(@PathParam("id") String jobid);



	@GET
	@Path("/job/{id}/finished")
	@Produces("text/plain")
	public Boolean jobFinished(@PathParam("id") String jobid);

	
	@POST
	@Path("/jobs/")
	@Produces("application/xml")
	public List<Job> listJobs();
	
	
	/**
	 * Gets a specific transcode job
	 */
	@GET
	@Path("/job/{id}")
	@Produces("application/xml")
	public Job job(@PathParam("id") String jobid);

	@GET
	@Path("/preset")
	@Produces("application/xml")
	public ArrayOfPreset listPresets();

	@GET
	@Path("/job/{id}/success")
	@Produces("text/plain")
	public Boolean jobSuccessful(@PathParam("id") String jobid);

	@GET
	@Path("/job/{id}/errormessage")
	public String jobErrorMessage(@PathParam("id") String jobid);
	
	
} 
