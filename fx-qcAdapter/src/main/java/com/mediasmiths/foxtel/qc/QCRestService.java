package com.mediasmiths.foxtel.qc;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;


@Path("/qc")
public interface QCRestService {

	/**
	 * Starts quality control for the request file using specified profile
	 * 
	 * ident is used to build to build the name of corresponding mediasets and jobs
	 * 
	 * @see QCStartStatus
	 * 
	 * @param file
	 * @param ident
	 * @param profileName
	 * @return
	 */
	@PUT
	@Path("/start")
	@Produces("application/xml")
	public QCStartResponse start(@QueryParam("file") String file,
			@QueryParam("ident") String ident,
			@QueryParam("profile") String profileName);

	/**
	 * Returns the status of a given qc job
	 * @param ident
	 * @return
	 */
	@GET
	@Path("/status")
	@Produces("application/xml")
	public QCStatus status(@QueryParam("identifier") QCIdentifier ident);
	
	/**
	 * May be used to query if a given profile exists, this may be useful for clients to validate their configuration on startup
	 * 
	 * @param profileName
	 * @return
	 */
	@GET
	@Path("/profileexists/{profile}")
	@Produces("application/xml")
	public Boolean profileExists(@PathParam("profile") String profileName);
	
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();

}
