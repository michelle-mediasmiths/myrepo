package com.mediasmiths.foxtel.qc;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;


@Path("/qc")
public interface QCRestService {

	@PUT
	@Path("/start")
	@Produces("application/xml")
	public QCStartResponse start(@QueryParam("file") String file,
			@QueryParam("ident") String ident,
			@QueryParam("profile") String profileName);

	@GET
	@Path("/status")
	@Produces("application/xml")
	public QCStatus status(@QueryParam("identifier") QCIdentifier filePath);
	
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();

}
