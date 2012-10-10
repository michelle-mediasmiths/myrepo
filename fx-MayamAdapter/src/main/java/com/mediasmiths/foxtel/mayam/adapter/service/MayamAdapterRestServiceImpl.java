package com.mediasmiths.foxtel.mayam.adapter.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public class MayamAdapterRestServiceImpl implements MayamAdapterRestService
{

	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping()
	{
		return "ping";
	}

}
