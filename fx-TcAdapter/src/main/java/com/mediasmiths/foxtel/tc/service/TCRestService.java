package com.mediasmiths.foxtel.tc.service;

import java.io.IOException;
import java.net.UnknownHostException;

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
	@Path("/start/")
	public String transcode(String jobName, String inputPath, String ouputPath, String profileName) throws UnknownHostException, TransformerException, ParserConfigurationException, IOException;
	

} 
