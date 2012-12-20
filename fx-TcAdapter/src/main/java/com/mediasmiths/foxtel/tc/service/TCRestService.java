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
import javax.ws.rs.QueryParam;

import org.datacontract.schemas._2004._07.rhozet.ArrayOfPreset;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.datacontract.schemas._2004._07.rhozet.JobStatus;

import com.mediasmiths.foxtel.carbonwfs.WfsClientException;
import com.mediasmiths.foxtel.tc.JobBuilderException;
import com.mediasmiths.foxtel.tc.model.TCBuildJobResponse;
import com.mediasmiths.foxtel.tc.model.TCBuildJobXMLRequest;
import com.mediasmiths.foxtel.tc.model.TCStartRequest;
import com.mediasmiths.mayam.MayamClientException;

@Path("/tc")
public interface TCRestService
{

	/**
	 * Simple ping method to check service is up
	 * 
	 * @return
	 */
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();

	/**
	 * Build the transcode job xml to be used for a package
	 * 
	 * @param buildJobXMLRequest
	 * @return
	 * @throws MayamClientException
	 * @throws JobBuilderException
	 */
	@POST
	@Path("/job/build/")
	@Produces("application/xml")
	public TCBuildJobResponse buildJobXMLForTranscode(TCBuildJobXMLRequest buildJobXMLRequest)
			throws MayamClientException,
			JobBuilderException;

	
	@POST
	@Path("/job/priority/")
	@Produces("text/plain")
	public Integer getPriorityForJob(TCBuildJobXMLRequest buildJobXMLRequest) throws MayamClientException, JobBuilderException;
	
	/**
	 * Starts a transcode job
	 * 
	 * @param startRequest
	 * @return
	 * @throws WfsClientException
	 */
	@PUT
	@Path("/job/start/")
	@Produces("text/plain")
	public UUID transcode(TCStartRequest startRequest) throws WfsClientException;

	/**
	 * Fetches the status of a given transcode job
	 * 
	 * @param jobid
	 * @return
	 */
	@GET
	@Path("/job/{id}/status")
	@Produces("application/xml")
	public JobStatus jobStatus(@PathParam("id") String jobid);

	/**
	 * Returns true if the specified job has finished
	 * 
	 * @param jobid
	 * @return
	 */
	@GET
	@Path("/job/{id}/finished")
	@Produces("text/plain")
	public Boolean jobFinished(@PathParam("id") String jobid);

	/**
	 * Lists transcode jobs
	 * 
	 * @return
	 */
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

	/**
	 * returns true if the given job has completed successfuly
	 * 
	 * @param jobid
	 * @return
	 */
	@GET
	@Path("/job/{id}/success")
	@Produces("text/plain")
	public Boolean jobSuccessful(@PathParam("id") String jobid);

	/**
	 * Returns any error message associated with the specified job
	 * @param jobid
	 * @return
	 */
	@GET
	@Path("/job/{id}/errormessage")
	public String jobErrorMessage(@PathParam("id") String jobid);

	
	@PUT
	@Path("/job/{id}/priority/set")
	public void setJobPriority(@PathParam("id") String jobid, @QueryParam("priority") Integer newPriority);
	
	/**
	 * Lists presets
	 * 
	 * @return
	 */
	@GET
	@Path("/preset")
	@Produces("application/xml")
	public ArrayOfPreset listPresets();

}
