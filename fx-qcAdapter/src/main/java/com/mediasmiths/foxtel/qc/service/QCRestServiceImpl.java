package com.mediasmiths.foxtel.qc.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.List;

import javassist.NotFoundException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.InternalServerErrorException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.cerify.CerifyClient;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocation;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocationNotFoundException;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocationService;
import com.mediasmiths.foxtel.pathresolver.PathResolver;
import com.mediasmiths.foxtel.qc.model.JobStatusType;
import com.mediasmiths.foxtel.qc.model.QCJobIdentifier;
import com.mediasmiths.foxtel.qc.model.QCJobResult;
import com.mediasmiths.foxtel.qc.model.QCJobStatus;
import com.mediasmiths.foxtel.qc.model.QCMediaResult;
import com.mediasmiths.foxtel.qc.model.QCStartRequest;
import com.mediasmiths.foxtel.qc.model.QCStartResponse;
import com.mediasmiths.foxtel.qc.model.QCStartStatus;
import com.tektronix.www.cerify.soap.client.BaseCeritalkFault;
import com.tektronix.www.cerify.soap.client.GetJobResultsResponse;
import com.tektronix.www.cerify.soap.client.GetJobStatusResponse;
import com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse;
import com.tektronix.www.cerify.soap.client.JobDoesntExistFault;
import com.tektronix.www.cerify.soap.client.JobIsArchivedFault;
import com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

public class QCRestServiceImpl implements QCRestService
{

	private final static Logger log = Logger.getLogger(QCRestServiceImpl.class);
	
	@Inject
	private CerifyClient cerifyClient;
	@Inject
	private PathResolver pathResolver;
	@Inject
	private MediaLocationService mediaLocations;

	@Override
	public QCJobStatus jobStatus(QCJobIdentifier job) throws QCAdapterException
	{

		log.debug(String.format("Status request for job %s", job.getIdentifier()));

		try
		{
			GetJobStatusResponse status = cerifyClient.getStatus(job.getIdentifier());
			log.debug(String.format(
					"Status of job %s is %s progress is %f ",
					job.getIdentifier(),
					status.getStatus().getValue(),
					status.getProgress().floatValue()));
			return new QCJobStatus(job, status.getStatus().getValue(), status.getProgress(), status.getAlertcount());
		}
		catch (JobDoesntExistFault e)
		{
			String message = String.format("Status request for job %s failed", job.getIdentifier());
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		catch (RemoteException e)
		{
			log.error(String.format("RemoteException querying status of job %s", job.getIdentifier()), e);
			throw new QCAdapterException(e);
		}
	}

	@Override
	public String ping()
	{
		return "ping";
	}

	@Override
	public QCStartResponse start(QCStartRequest request) throws QCAdapterException
	{

		log.debug(String.format("Start requested for file  %s", request.getFile()));

		MediaLocation location;
		try
		{
			location = mediaLocations.getLocationForAbsoluteFilePath(request.getFile());
		}
		catch (MediaLocationNotFoundException e)
		{
			log.error("error getting media location for file",e);
			throw new QCAdapterException("error getting media location for file",e);
		}
		
		String relativePathToFile = pathResolver.getRelativePath(location.getPath(), request.getFile());
	
		String proirity = "Medium";
		if(request.getPriority() != null){
			proirity=request.getPriority();
		}
		else{
			log.warn("requested qc priority is was null");
		}
		
		String jobName;
		try
		{
			jobName = cerifyClient.startQcForFile(relativePathToFile, request.getIdent(), request.getProfileName(),location,proirity);
		}
		catch (MalformedURIException | RemoteException | MediaLocationNotFoundException e)
		{
			log.error("error starting qc for file",e);
			throw new QCAdapterException("error starting qc for file",e);
		}
		log.info(String.format("Job %s created", jobName));
		QCStartResponse res = new QCStartResponse(QCStartStatus.STARTED);
		QCJobIdentifier jobIdent = new QCJobIdentifier(jobName);
		jobIdent.setProfile(request.getProfileName());
		res.setQcIdentifier(jobIdent);
		return res;

	}

	@Override
	public QCJobResult jobResult(QCJobIdentifier ident) throws QCAdapterException
	{

		log.debug(String.format("Result requested for job %s", ident.getIdentifier()));
		try
		{
			GetJobResultsResponse jobResults = cerifyClient.getJobResult(ident.getIdentifier());

			QCJobResult qcr = new QCJobResult();
			qcr.setIdent(new QCJobIdentifier(jobResults.getName(), jobResults.getProfile()));
			String resultString = jobResults.getResult().getValue();
			log.debug("Result is " + resultString);
			qcr.setResult(resultString);
			qcr.setCompleted(jobResults.getCompleted());
			qcr.setUrl(new java.net.URI(jobResults.getUrl().toString()));

			log.debug("Returning job result: " + qcr.toString());
			return qcr;

		}
		catch (JobDoesntExistFault e)
		{
			String message = String.format("Result request for job %s failed", ident.getIdentifier());
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		catch (RemoteException e)
		{
			log.error("remote exception querying job result for job " + ident, e);
			throw new QCAdapterException(e);
		}
		catch (URISyntaxException e)
		{
			log.error("A uri returned by the cerify api is malformed", e);
			throw new QCAdapterException(e);
		}

	}

	@Override
	public Boolean profileExists(String profileName)
	{

		log.debug(String.format("Profile Exists request for %s", profileName));
		List<String> listProfiles;
		try
		{
			listProfiles = cerifyClient.listProfiles();
			if (listProfiles.contains(profileName))
			{
				log.debug(String.format("Profile %s exists", profileName));
				return Boolean.valueOf(true);
			}
			else
			{
				log.debug(String.format("Profile %s does not exist", profileName));
				return Boolean.valueOf(false);
			}

		}
		catch (RemoteException e)
		{
			log.error("remote exception querying profile existance ", e);
			throw new InternalServerErrorException(e);
		}
	}

	@Override
	public QCMediaResult mediaResult(String file, String jobName, Integer runNumber) throws QCAdapterException
	{

		QCJobIdentifier ident = new QCJobIdentifier(jobName);

		log.debug(String.format("Media result for file %s and job %s requested", file, ident.getIdentifier()));

		try
		{
			
			MediaLocation location;
			try
			{
				location = mediaLocations.getLocationForAbsoluteFilePath(file);
			}
			catch (MediaLocationNotFoundException e)
			{
				log.error("error getting media location for file",e);
				throw new QCAdapterException("error getting media location for file",e);				
			}
			
			String relativePathToFile = pathResolver.getRelativePath(location.getPath(),file);
		
			GetMediaFileResultsResponse res;
			try
			{
				res = cerifyClient.getMediaResult(relativePathToFile, ident.getIdentifier(),location, runNumber.intValue());
			}
			catch (MediaLocationNotFoundException e)
			{
				log.error("error getting media result",e);
				throw new QCAdapterException("error getting media result",e);
			}
			
			//TODO: include serialized full report!
			
			QCMediaResult mediaResult = new QCMediaResult();
			
			log.info("returning result of "+res.getResult().getValue());
			
			mediaResult.setResult(res.getResult().getValue());
			mediaResult.setUrl(new URI(res.getUrl().toString()));

			return mediaResult;

		}
		catch (JobDoesntExistFault e)
		{
			String message = String.format("Media Result request for job %s failed", ident.getIdentifier());
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		catch (MediaFileNotInJobFault e)
		{
			String message = String.format("Media %s is not part of job %s ", file, ident.getIdentifier());
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		catch (RemoteException e)
		{
			log.error("remote exception requesting media qc result ", e);
			throw new QCAdapterException(e);
		}
		catch (MalformedURIException e)
		{
			log.error("The cerify client has produced a malformed uri", e);
			throw new QCAdapterException(e);
		}
		catch (URISyntaxException e)
		{
			log.error("A uri returned by the cerify api is malformed", e);
			throw new QCAdapterException(e);
		}
	}

	@Override
	@GET
	@Path("/job/{identifier}/finished")
	@Produces("text/plain")
	public Boolean jobFinished(@PathParam("identifier") QCJobIdentifier ident) throws QCAdapterException
	{
		log.debug(String.format("Finished request for job %s", ident.getIdentifier()));

		QCJobStatus jobStatus = jobStatus(ident);

		return jobStatus.getStatus() == JobStatusType.complete || jobStatus.getStatus() == JobStatusType.stopping;
	}

	@Override
	@DELETE
	@Path("/job/{jobname}/cancel")
	public void cancelJob(@PathParam("jobname") String jobName) throws QCAdapterException
	{
		log.info(String.format("Cancel requested for job %s",jobName));
		try
		{
			cerifyClient.cancelJob(jobName);
		}
		catch (JobDoesntExistFault e)
		{
			String message = String.format("Cancel request for job %s failed (JobDoesntExistFault)", jobName);
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		catch (JobIsArchivedFault e)
		{
			String message = String.format("Cancel request for job %s failed (JobIsArchivedFault)", jobName);
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		catch (BaseCeritalkFault e)
		{
			String message = String.format("Cancel request for job %s failed (BaseCeritalkFault)", jobName);
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		catch (RemoteException e)
		{
			String message = String.format("Cancel request for job %s failed (RemoteException)", jobName);
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		
	}
}
