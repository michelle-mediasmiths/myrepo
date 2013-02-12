package com.mediasmiths.foxtel.cerify;


import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocation;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocationNotFoundException;
import com.mediasmiths.foxtel.cerify.medialocation.MediaLocationService;
import com.tektronix.www.cerify.soap.client.BaseCeritalkFault;
import com.tektronix.www.cerify.soap.client.CeriTalk_PortType;
import com.tektronix.www.cerify.soap.client.ControlJob;
import com.tektronix.www.cerify.soap.client.ControlJobResponse;
import com.tektronix.www.cerify.soap.client.CreateJob;
import com.tektronix.www.cerify.soap.client.CreateMediaSet;
import com.tektronix.www.cerify.soap.client.GetJobResultsResponse;
import com.tektronix.www.cerify.soap.client.GetJobStatus;
import com.tektronix.www.cerify.soap.client.GetJobStatusResponse;
import com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse;
import com.tektronix.www.cerify.soap.client.GetProfiles;
import com.tektronix.www.cerify.soap.client.GetProfilesResponse;
import com.tektronix.www.cerify.soap.client.JobActionType;
import com.tektronix.www.cerify.soap.client.JobDoesntExistFault;
import com.tektronix.www.cerify.soap.client.JobIsArchivedFault;
import com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;
import com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault;
import com.tektronix.www.cerify.soap.client.MediaSetNameInUseFault;
import com.tektronix.www.cerify.soap.client.PriorityType;
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
	public String startQcForFile(String file, String ident, String profileName, MediaLocation location, String priority) throws MalformedURIException, RemoteException, MediaLocationNotFoundException
	{
		log.info("QC start request for " + file);
		
		String mediaSetName = mediasetName(file, ident);

		// if the media set name specified is already in use then a new media set name may be returned
		mediaSetName = createMediaSet(file, ident, mediaSetName,location,0);

		String jobName = jobName(profileName, mediaSetName);
		PriorityType priorityType = PriorityType.fromString(priority);

		log.info(String.format("Creating a job %s", jobName));
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
	public GetJobStatusResponse getStatus(String jobName) throws JobDoesntExistFault, BaseCeritalkFault, RemoteException
	{
		log.info(String.format("Getting status of job %s", jobName));
		GetJobStatus request = new GetJobStatus(jobName);
		GetJobStatusResponse jobStatus = service.getJobStatus(request);
		return jobStatus;
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
	public GetJobResultsResponse getJobResult(String jobName) throws JobDoesntExistFault, BaseCeritalkFault, RemoteException
	{
		log.info(String.format("Getting results of job %s", jobName));
		GetJobResults request = new GetJobResults(jobName);
		GetJobResultsResponse jobResults = service.getJobResults(request);
		return jobResults;
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
	public GetMediaFileResultsResponse getMediaResult(String filePath, String jobName, MediaLocation mediaLocation, int runNumber)
			throws MalformedURIException,
			JobDoesntExistFault,
			MediaFileNotInJobFault,
			BaseCeritalkFault,
			RemoteException, MediaLocationNotFoundException
	{
		log.info(String.format("Getting results of media %s in job %s", filePath, jobName));

		URI url = mediaLocation.resolveUriForFile(filePath);
		GetMediaFileResults request = new GetMediaFileResults(jobName, url, 0);
		GetMediaFileResultsResponse mediaFileResults = service.getMediaFileResults(request);
		return mediaFileResults;
	}

	/**
	 * Attempts to cancel the job with the given name
	 * @param jobName
	 * @throws JobDoesntExistFault
	 * @throws JobIsArchivedFault
	 * @throws BaseCeritalkFault
	 * @throws RemoteException
	 */
	public void cancelJob(String jobName) throws JobDoesntExistFault, JobIsArchivedFault, BaseCeritalkFault, RemoteException{
		
			log.info(String.format("Cancelling job %s",jobName));
			ControlJob control = new ControlJob(jobName, JobActionType.stopnow);
			service.controlJob(control);					
	}
	
	private String jobName(String profileName, String mediaSetName)
	{
		String jobName = mediaSetName + "_" + profileName;
		return jobName;
	}

	private String mediasetName(String file, String ident)
	{
		String mediaSetName = FilenameUtils.getBaseName(file) + "_" + ident;
		return mediaSetName;
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

		
		URI media = mediaLocation.resolveUriForFile(file);

		CreateMediaSet cms;
		if (mediaSetNameSuffix == 0) // mediaSetNameSuffix > 0 implies the suffix should be used to try and create and unique media set name
		{
			cms = new CreateMediaSet(mediaSetName, mediaLocation.getName(), media);
		}
		else
		{
			cms = new CreateMediaSet(mediaSetName + "_" + mediaSetNameSuffix, mediaLocation.getName(), media);
		}
		log.debug("Sending CreateMediaSetRequest");
		try
		{
			service.createMediaSet(cms);
		}
		catch (MediaSetNameInUseFault e)
		{
			log.info("Media set name already in use", e);
			return createMediaSet(file, ident, mediaSetName,mediaLocation, mediaSetNameSuffix + 1);
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
		
		return resolveMediaSetName(mediaSetName, mediaSetNameSuffix);
		
	}

	private String resolveMediaSetName(String mediaSetName, int mediaSetNameSuffix)
	{
		if (mediaSetNameSuffix == 0)
		{
			return mediaSetName; //the specified mediaset name was available
		}
		else
		{
			//a suffix was required to generate a unique mediaset name
			return mediaSetName + "_" + mediaSetNameSuffix;
		}
	}

	

}
