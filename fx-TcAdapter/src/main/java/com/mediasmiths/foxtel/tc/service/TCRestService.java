package com.mediasmiths.foxtel.tc.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.mediasmiths.foxtel.carbon.jaxb.Job;
import com.mediasmiths.foxtel.carbon.jaxb.Profile;

@Path("/tc")
public interface TCRestService
{
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();

	@PUT
	@Path("/job/start")
	public String transcode(@QueryParam("jobname") String jobName,
			@QueryParam("input") String inputPath,
			@QueryParam("output") String ouputPath,
			@QueryParam("profile") String profileName) throws UnknownHostException, TransformerException, ParserConfigurationException, IOException;
	
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
	@Path("/job")
	@Produces("application/xml")
	public List<Job> listJobs() throws TransformerException, ParserConfigurationException, UnknownHostException, IOException, JAXBException;
	
	

	/**
	 * Lists available transcode profiles
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws JAXBException 
	 */
	@GET
	@Path("/profiles")
	@Produces("application/xml")
	public List<Profile> listProfiles() throws TransformerException, ParserConfigurationException, UnknownHostException, IOException, JAXBException;
	
} 
