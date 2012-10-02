package com.mediasmiths.foxtel.qc.service;

import java.io.File;

import javassist.NotFoundException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.mediasmiths.foxtel.qc.QCJobIdentifier;
import com.mediasmiths.foxtel.qc.QCJobResult;
import com.mediasmiths.foxtel.qc.QCJobStatus;
import com.mediasmiths.foxtel.qc.QCMediaResult;
import com.mediasmiths.foxtel.qc.QCStartResponse;
import com.mediasmiths.foxtel.qc.QCStartStatus;

@Path("/qc")
public interface QCRestService {

	/**
	 * Starts quality control for the request file using specified profile
	 * 
	 * ident is used to build to build the name of corresponding mediaset and
	 * job
	 * 
	 * @see QCStartStatus
	 * 
	 * @param file
	 *            - the file to be qc'ed
	 * @param ident
	 *            - an identifier to use when generating the qc jobs name
	 * @param profileName
	 *            - the qc profile to check file against
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
	 * 
	 * @param ident
	 *            - an identifier for the job
	 * @return the status of the job
	 * @throws NotFoundException
	 *             if the specified job does not exist
	 */
	@GET
	@Path("/job/{identifier}/status")
	@Produces("application/xml")
	public QCJobStatus jobStatus(@PathParam("identifier") QCJobIdentifier ident)
			throws NotFoundException;

	/**
	 * Returns the result of a given job
	 * 
	 * @param ident
	 *            - the jobs identifier
	 * @return
	 * @throws NotFoundException
	 *             - if the job does not exist
	 */
	@GET
	@Path("/job/{identifier}/result")
	@Produces("application/xml")
	public QCJobResult jobResult(@PathParam("identifier") QCJobIdentifier ident)
			throws NotFoundException;

	/**
	 * Returns the result of the given media as part of the given job
	 * 
	 * This is distinct from jobResult as there may be multiple media files
	 * processed by a given job (though it makes life simpler if we just process
	 * one media file per job)
	 * 
	 * @throws NotFoundException
	 * 
	 */
	@GET
	@Path("/job/{jobname}/result/file")
	@Produces("application/xml")
	public QCMediaResult mediaResult(@QueryParam("path") String file,
			@PathParam("jobname") String jobName,
			@QueryParam("run") @DefaultValue("0") Integer runNumber)
			throws NotFoundException;

	/**
	 * Used to query if a given profile exists, this may be useful for
	 * clients to validate their configuration on startup
	 * 
	 * @param profileName
	 * @return
	 */
	@GET
	@Path("/profile/{profile}/exists")
	@Produces("text/plain")
	public Boolean profileExists(@PathParam("profile") String profileName);

	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();

}