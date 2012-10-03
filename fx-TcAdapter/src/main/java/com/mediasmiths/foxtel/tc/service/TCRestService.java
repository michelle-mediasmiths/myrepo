package com.mediasmiths.foxtel.tc.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

@Path("/tc")
public interface TCRestService
{
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();

	@PUT
	@Path("/start")
	public String transcode(@QueryParam("jobname") String jobName,
			@QueryParam("input") String inputPath,
			@QueryParam("output") String ouputPath,
			@QueryParam("profile") String profileName) throws UnknownHostException, TransformerException, ParserConfigurationException, IOException;
	

	/**
	 * Lists available transcode profiles
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	@GET
	@Path("/profiles")
	@Produces("text/plain")
	public List<String> listProfiles() throws TransformerException, ParserConfigurationException, UnknownHostException, IOException;
	
} 
