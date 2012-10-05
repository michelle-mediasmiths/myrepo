package com.mediasmiths.foxtel.tc.service;

import java.io.IOException;
import java.net.UnknownHostException;
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
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.datacontract.schemas._2004._07.rhozet.JobStatus;

import com.mediasmiths.foxtel.carbonwfs.WfsClientException;


@Path("/tc")
public interface TCRestService
{
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();

	@PUT
	@Path("/job/start")
	public UUID transcode(@QueryParam("jobname") String jobName,
			@QueryParam("input") String inputPath,
			@QueryParam("output") String ouputPath,
			@QueryParam("profile") UUID profileName) throws WfsClientException;
	
	/**
	 * Lists all transcode jobs
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws JAXBException 
	 */
	@GET
	@Path("/job/{id}/status")
	@Produces("application/xml")
	public JobStatus jobStatus(@PathParam("id") UUID jobid);
	
	/**
	 * Creates the profile described by profileXML
	 * @param profileXML
	 * @return the id of the created profile
	 */
	@PUT
	@Path("/profile/create")
	@Consumes("application/x-www-form-urlencoded")
	public UUID createProfile(@FormParam("profile") String profileXML);
	
} 
