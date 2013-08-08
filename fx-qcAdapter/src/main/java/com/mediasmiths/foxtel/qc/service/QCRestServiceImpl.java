package com.mediasmiths.foxtel.qc.service;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.cerify.CerifyClient;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocation;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocationNotFoundException;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocationService;
import com.mediasmiths.foxtel.pathresolver.PathResolver;
import com.mediasmiths.foxtel.qc.model.JobStatusType;
import com.mediasmiths.foxtel.qc.model.QCCleanupResponse;
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
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.InternalServerErrorException;

import javax.ws.rs.PathParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.List;

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
	public QCJobStatus jobStatus(final QCJobIdentifier job) throws QCAdapterException
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
			final String message = String.format("Status request for job %s failed", job.getIdentifier());
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
	public QCStartResponse start(final QCStartRequest request) throws QCAdapterException
	{
		log.debug(String.format("QC Start requested for file %s", request.getFile()));

		MediaLocation location;
		try
		{
			location = mediaLocations.getLocationForAbsoluteFilePath(request.getFile());
		}
		catch (MediaLocationNotFoundException e)
		{
			log.error("error getting media location for file", e);
			throw new QCAdapterException("error getting media location for file", e);
		}
		
		final String relativePathToFile = pathResolver.getRelativePath(location.getPath(), request.getFile());
		log.debug(String.format("Relative path to file returned %s", relativePathToFile));
	
		String priority;
		if(request.getPriority() != null)
		{
			priority = request.getPriority();
			log.debug(String.format("Priority for job set to %s", priority));
		}
		else
		{
			log.warn("Requested qc priority was null; setting priority to Medium");
			priority = "Medium";
		}
		
		String jobName;
		try
		{
			jobName = cerifyClient.startQcForFile(relativePathToFile, request.getIdent(), request.getProfileName(),location,priority);
		}
		catch (MalformedURIException | RemoteException | MediaLocationNotFoundException e)
		{
			log.error("Error starting qc for file", e);
			throw new QCAdapterException("Error starting qc for file", e);
		}
		
		log.info(String.format("Job %s created", jobName));
		
		QCStartResponse res = new QCStartResponse(QCStartStatus.STARTED);
		QCJobIdentifier jobIdent = new QCJobIdentifier(jobName);
		jobIdent.setProfile(request.getProfileName());
		res.setQcIdentifier(jobIdent);
		return res;
	}


	@Override
	public QCCleanupResponse cleanup(@PathParam("jobname") final String jobName) throws QCAdapterException
	{
		log.debug(String.format("Cleanup requested for job %s", jobName));

		try
		{
			cerifyClient.cleanupJobAndMediasets(jobName);
			return new QCCleanupResponse(true);
		}
		catch (RemoteException e)
		{
			log.error("Error cleaning up job",e);
			throw new QCAdapterException(e);
		}

	}


	@Override
	public QCJobResult jobResult(final QCJobIdentifier ident) throws QCAdapterException
	{
		log.debug(String.format("Result requested for job %s", ident.getIdentifier()));
		try
		{
			final GetJobResultsResponse jobResults = cerifyClient.getJobResult(ident.getIdentifier());

			final QCJobResult qcr = new QCJobResult();
			qcr.setIdent(new QCJobIdentifier(jobResults.getName(), jobResults.getProfile()));
			final String resultString = jobResults.getResult().getValue();
			log.debug("Result is " + resultString);
			qcr.setResult(resultString);
			qcr.setCompleted(jobResults.getCompleted());
			qcr.setUrl(new java.net.URI(jobResults.getUrl().toString()));

			log.debug("Returning job result: " + qcr.toString());
			return qcr;
		}
		catch (JobDoesntExistFault e)
		{
			final String message = String.format("Result request for job %s failed", ident.getIdentifier());
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		catch (RemoteException e)
		{
			log.error(String.format("Remote exception querying job result for job %s", ident), e);
			throw new QCAdapterException(e);
		}
		catch (URISyntaxException e)
		{
			log.error("A uri returned by the cerify api is malformed", e);
			throw new QCAdapterException(e);
		}
	}

	@Override
	public Boolean profileExists(final String profileName)
	{
		log.debug(String.format("Profile Exists request for %s", profileName));
		
		try
		{
			List<String> listProfiles = cerifyClient.listProfiles();
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
			log.error("Remote exception querying profile existance ", e);
			throw new InternalServerErrorException(e);
		}
	}

	@Override
	public QCMediaResult mediaResult(final String file, final String jobName, final Integer runNumber) throws QCAdapterException
	{
		log.debug(String.format("Media result for file %s and job %s requested with runNumber %d", file, jobName, runNumber));
		QCJobIdentifier ident = new QCJobIdentifier(jobName);
		
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
			
			final String relativePathToFile = pathResolver.getRelativePath(location.getPath(), file);
			log.debug(String.format("Relative path to file is %s", relativePathToFile));
		
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
			final String resultValue = res.getResult().getValue();
			log.info("returning result of "+ resultValue);
			
			QCMediaResult mediaResult = new QCMediaResult();
			mediaResult.setResult(resultValue);
			mediaResult.setUrl(new URI(res.getUrl().toString()));

			return mediaResult;
		}
		catch (JobDoesntExistFault e)
		{
			final String message = String.format("Media Result request for job %s failed", ident.getIdentifier());
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		catch (MediaFileNotInJobFault e)
		{
			final String message = String.format("Media %s is not part of job %s ", file, ident.getIdentifier());
			log.warn(message, e);
			throw new QCAdapterException(message, e);
		}
		catch (RemoteException e)
		{
			log.error("Remote exception requesting media qc result ", e);
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
	public Boolean jobFinished(final QCJobIdentifier ident) throws QCAdapterException
	{
		log.debug(String.format("Finished request for job %s", ident.getIdentifier()));

		QCJobStatus jobStatus = jobStatus(ident);

		return jobStatus.getStatus() == JobStatusType.complete || jobStatus.getStatus() == JobStatusType.stopping;
	}

	@Override
	public void cancelJob(final String jobName) throws QCAdapterException
	{
		log.info(String.format("Cancel requested for job %s", jobName));
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
		log.debug(String.format("Call to cancel job request completed without exceptions for job %s", jobName));
	}
}
