package com.mediasmiths.foxtel.cerify;


import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocation;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocationNotFoundException;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocationService;
import com.tektronix.www.cerify.soap.client.BaseCeritalkFault;
import com.tektronix.www.cerify.soap.client.CeriTalk_PortType;
import com.tektronix.www.cerify.soap.client.ControlJob;
import com.tektronix.www.cerify.soap.client.ControlMediaSet;
import com.tektronix.www.cerify.soap.client.ControlMediaSetResponse;
import com.tektronix.www.cerify.soap.client.CreateJob;
import com.tektronix.www.cerify.soap.client.GetJobs;
import com.tektronix.www.cerify.soap.client.CreateMediaSet;
import com.tektronix.www.cerify.soap.client.GetJobResultsResponse;
import com.tektronix.www.cerify.soap.client.GetJobStatus;
import com.tektronix.www.cerify.soap.client.GetJobStatusResponse;
import com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse;
import com.tektronix.www.cerify.soap.client.GetProfiles;
import com.tektronix.www.cerify.soap.client.GetProfilesResponse;
import com.tektronix.www.cerify.soap.client.GetJobsResponse;
import com.tektronix.www.cerify.soap.client.JobActionType;
import com.tektronix.www.cerify.soap.client.JobDoesntExistFault;
import com.tektronix.www.cerify.soap.client.JobIsArchivedFault;
import com.tektronix.www.cerify.soap.client.JobStatusType;
import com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;
import com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault;
import com.tektronix.www.cerify.soap.client.MediaSetActionType;
import com.tektronix.www.cerify.soap.client.MediaSetNameInUseFault;
import com.tektronix.www.cerify.soap.client.InvalidRangeFault;
import com.tektronix.www.cerify.soap.client.PriorityType;
import com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault;
import com.tektronix.www.cerify.soap.client.URLNotAccessibleFault;
import com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault;
import com.tektronix.www.cerify.soap.client._20101220.GetJobResults;
import com.tektronix.www.cerify.soap.client._20101220.GetMediaFileResults;

@Singleton
public class CerifyClient
{

	private final static Logger log = Logger.getLogger(CerifyClient.class);

	@Inject
	private CeriTalk_PortType service;

	@Inject
	private MediaLocationService mediaLocations;
	
	/**
	 * Starts QC for a given file, using the provided identifier as a basis for the names of the created MediaSet and Job
	 * 
	 * @param f
	 * @param ident
	 * @return
	 * @throws MalformedURIException
	 * @throws RemoteException
	 * @throws MediaLocationNotFoundException 
	 */
	public String startQcForFile(final String file, final String ident, final String profileName, final MediaLocation location, final String priority)
			throws MalformedURIException, 
			RemoteException, 
			MediaLocationNotFoundException
	{
		log.info("QC start request for " + file);
		
		if(log.isDebugEnabled())
		{
			log.debug(String.format("QC start request for file %s; ident %s; profileName %s; mediaLocation %s; priority %s", file, ident, profileName, location.getPath(), priority));
		}
		
		// if the media set name specified is already in use then a new media set name may be returned
		final String mediaSetName = createMediaSet(file, ident, mediasetName(file, ident), location, 0);
		
		log.debug(String.format("mediaSetName is %s", mediaSetName));

		final String jobName = jobName(profileName, mediaSetName);
		
		final PriorityType priorityType = PriorityType.fromString(priority);

		log.info(String.format("Creating a job %s with priority %s", jobName, priority));
		CreateJob createJob = new CreateJob(jobName, mediaSetName, profileName, priorityType);
		
		log.debug("Sending CreateJobRequest");
		service.createJob(createJob);

		return jobName;
	}

	/**
	 * Returns a list of profile names
	 * 
	 * @return
	 * @throws BaseCeritalkFault
	 * @throws RemoteException
	 */
	public List<String> listProfiles() throws BaseCeritalkFault, RemoteException
	{
		log.info("listing profiles");
		GetProfiles request = new GetProfiles();
		GetProfilesResponse response = service.getProfiles(request);

		String[] profileName = response.getProfileName();
		return Arrays.asList(profileName);
	}

	/**
	 * Returns the (string) status of a job, one of waiting processing complete stopping stopped
	 * 
	 * @see GetJobStatusResponse
	 * 
	 * @param jobName
	 * @return
	 * @throws JobDoesntExistFault
	 * @throws BaseCeritalkFault
	 * @throws RemoteException
	 */
	public GetJobStatusResponse getStatus(final String jobName) throws JobDoesntExistFault, BaseCeritalkFault, RemoteException
	{
		log.info(String.format("Getting status of job %s", jobName));
		GetJobStatus request = new GetJobStatus(jobName);
		return service.getJobStatus(request);
	}

	/**
	 * Returns the results of a job
	 * 
	 * @see GetJobResultsResponse
	 * 
	 * @param jobName
	 * @return
	 * @throws JobDoesntExistFault
	 * @throws BaseCeritalkFault
	 * @throws RemoteException
	 */
	public GetJobResultsResponse getJobResult(final String jobName) throws JobDoesntExistFault, BaseCeritalkFault, RemoteException
	{
		log.info(String.format("Getting results of job %s", jobName));
		GetJobResults request = new GetJobResults(jobName);
		return service.getJobResults(request);
	}

	/**
	 * Get the status for a given media file run as part of the specified job
	 * 
	 * @param filePath
	 * @param jobName
	 * @throws MalformedURIException
	 * @throws RemoteException
	 * @throws BaseCeritalkFault
	 * @throws MediaFileNotInJobFault
	 * @throws JobDoesntExistFault
	 * @throws MediaLocationNotFoundException 
	 */
	public GetMediaFileResultsResponse getMediaResult(final String filePath, final String jobName, final MediaLocation mediaLocation, final int runNumber)
			throws MalformedURIException,
			JobDoesntExistFault,
			MediaFileNotInJobFault,
			BaseCeritalkFault,
			RemoteException, MediaLocationNotFoundException
	{
		log.info(String.format("Getting results of media %s in job %s", filePath, jobName));

		if(log.isDebugEnabled())
		{
			log.debug(String.format("Getting results of media %s in job %s; mediaLocation %s; runNumber %d", 
					filePath, jobName, mediaLocation.getPath(), runNumber));
		}
		
		URI url = mediaLocation.resolveUriForFile(filePath);
		GetMediaFileResults request = new GetMediaFileResults(jobName, url, 0);
		log.debug("Calling service to getMediaFileResults");
		return service.getMediaFileResults(request);
	}

	/**
	 * Attempts to cancel the job with the given name
	 * @param jobName
	 * @throws JobDoesntExistFault
	 * @throws JobIsArchivedFault
	 * @throws BaseCeritalkFault
	 * @throws RemoteException
	 */
	public void cancelJob(final String jobName) 
			throws JobDoesntExistFault, JobIsArchivedFault, BaseCeritalkFault, RemoteException
	{	
			log.info(String.format("Cancelling job %s",jobName));
			ControlJob control = new ControlJob(jobName, JobActionType.stopnow);
			service.controlJob(control);					
	}
	
	/**
	 * Get all jobs based on status and date range (optional)
	 * @param status
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @throws InvalidRangeFault
	 * @throws BaseCeritalkFault
	 * @throws RemoteException
	 */
	public GetJobsResponse getJobs(String status, Calendar fromDate, Calendar toDate) throws InvalidRangeFault, BaseCeritalkFault, RemoteException
	{
		log.info("Start getJobs");
		
		GetJobs request = new GetJobs();
		JobStatusType requiredStatus = JobStatusType.fromValue(status);
		request.setStatus(requiredStatus);
		Calendar todayDate = Calendar.getInstance();
		
		fromDate = (fromDate==null)? todayDate: fromDate;
		toDate = (toDate==null)? todayDate: toDate;
		request.setCreateTimeRangeFrom(fromDate);
		request.setCreateTimeRangeTo(toDate);
		
		GetJobsResponse response = null;
		try
		{
			response = service.getJobs(request);
		}
		catch (InvalidRangeFault e)
		{
			log.error("Invalid date range error, e");
			throw e;
		}
		catch (BaseCeritalkFault e)
		{
			log.error("Error getting cerify jobs", e);
			throw e;
		}
		catch (RemoteException e)
		{
			log.error("Error getting cerify jobs", e);
			throw e;
		}
		
		return response;
		
	}
	
	/**
	 * Delete the mediaset name provided in the parameter
	 * @param mediasetName
	 * @return
	 * @throws MediaSetDoesntExistFault
	 * @throws BaseCeritalkFault
	 * @throws RemoteException
	 */
	public ControlMediaSetResponse deleteMediaset(String mediasetName) throws MediaSetDoesntExistFault, BaseCeritalkFault, RemoteException
	{
		log.info(String.format("Start delete mediaset %s" + mediasetName));
		ControlMediaSet request = new ControlMediaSet();
		request.setMediaSetName(mediasetName);
		request.setAction(MediaSetActionType.delete);
		ControlMediaSetResponse response = null;
		
		try
		{
			response = service.controlMediaSet(request);
		
		}
		catch (MediaSetDoesntExistFault e)
		{
			log.error("Error deleting mediaset: mediaset does not exsit " + mediasetName, e);
			throw e;
		}
		catch (BaseCeritalkFault e)
		{
			log.error("Error deleting mediaset", e);
			throw e;
		}
		catch (RemoteException e)
		{
			log.error("Error deleting mediaset", e);
			throw e;
		}
		return response;
	}
	
	private String jobName(final String profileName, final String mediaSetName)
	{
		return new StringBuilder(mediaSetName).append("_").append(profileName).toString();
	}

	private String mediasetName(final String file, final String ident)
	{
		return FilenameUtils.getBaseName(file) + "_" + ident;
	}

	private String createMediaSet(final String file, final String ident, final String mediaSetName, final MediaLocation mediaLocation, final int mediaSetNameSuffix)
			throws MalformedURIException,
			URLNotInMediaLocationFault,
			MediaLocationDoesntExistFault,
			MediaSetNameInUseFault,
			URLNotAccessibleFault,
			BaseCeritalkFault,
			RemoteException
	{
		log.info(String.format("Creating a mediaset %s", resolveMediaSetName(mediaSetName, mediaSetNameSuffix)));
		
		if(log.isDebugEnabled())
		{
			log.debug(String.format("Creating a mediaset for file %s; ident %s; mediaSetName %s; mediaLocation %s; mediaSetNameSuffix %d", 
					file, ident, mediaSetName, mediaLocation.getPath(), mediaSetNameSuffix));
		}

		final URI media = mediaLocation.resolveUriForFile(file);	
		
		final String resolvedMediaSetName = resolveMediaSetName(mediaSetName, mediaSetNameSuffix);
		
		final CreateMediaSet cms = 
				new CreateMediaSet(resolvedMediaSetName, mediaLocation.getName(), media);
		
		log.debug("Sending CreateMediaSetRequest");
		try
		{
			service.createMediaSet(cms);
		}
		catch (MediaSetNameInUseFault e)
		{
			log.info("Media set name already in use", e);
			return createMediaSet(file, ident, mediaSetName, mediaLocation, mediaSetNameSuffix + 1);
		}
		catch (URLNotInMediaLocationFault e)
		{
			log.error("Error creating mediaset", e);
			throw e;
		}
		catch (MediaLocationDoesntExistFault e)
		{
			log.error("Error creating mediaset", e);
			throw e;
		}
		catch (URLNotAccessibleFault e)
		{
			log.error("Error creating mediaset", e);
			throw e;
		}
		catch (BaseCeritalkFault e)
		{
			log.error("Error creating mediaset", e);
			throw e;
		}
		catch (RemoteException e)
		{
			log.error("Error creating mediaset", e);
			throw e;
		}
		
		return resolvedMediaSetName;
		
	}

	private String resolveMediaSetName(String mediaSetName, int mediaSetNameSuffix)
	{
		log.debug(String.format("Resolving mediaSetName %s; suffix %d", mediaSetName, mediaSetNameSuffix));
		
		final String mediaName = (mediaSetNameSuffix == 0) ? mediaSetName : 
			new StringBuilder(mediaSetName).append("_").append(mediaSetNameSuffix).toString();
		
		log.debug(String.format("Returning resolvedMediaSetName of %s", mediaName));
		return mediaName;
	}
}
