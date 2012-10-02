package com.mediasmiths.foxtel.tc.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/tc")
public interface TCRestService
{
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();
} 
