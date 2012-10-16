package com.mediasmiths.foxtel.tc.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.datacontract.schemas._2004._07.rhozet.ArrayOfPreset;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.datacontract.schemas._2004._07.rhozet.JobStatus;
import org.datacontract.schemas._2004._07.rhozet.Preset;

import com.mediasmiths.foxtel.carbonwfs.WfsClientException;
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
	@Produces("application/xml")
	public String buildJobXMLForTranscode(TCBuildJobXMLRequest buildJobXMLRequest) throws MayamClientException;
	
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
	
} 
