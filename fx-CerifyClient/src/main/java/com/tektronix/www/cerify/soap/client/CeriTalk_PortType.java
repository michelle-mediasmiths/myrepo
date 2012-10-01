/**
 * CeriTalk_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

import javax.annotation.Generated;

@Generated(value = {"axis"})public interface CeriTalk_PortType extends java.rmi.Remote {

    /**
     * Retrieve the test results for a given MediaFile
     *                 (specified by url) within a given Job (specified by
     * jobName). If complete results are required, use the
     *                 GetMediaFileStatus operation to ensure that the
     *                 MediaFile has a status of "complete" before
     *                 requesting the results.
     */
    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse getMediaFileResults(com.tektronix.www.cerify.soap.client._20101220.GetMediaFileResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

    /**
     * Retrieve the test results for a given MediaFile
     *                 (specified by url) within a given Job (specified by
     * jobName). If complete results are required, use the
     *                 GetMediaFileStatus operation to ensure that the
     *                 MediaFile has a status of "complete" before
     *                 requesting the results.
     */
    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse getMediaFileResults_Deprecated(com.tektronix.www.cerify.soap.client.GetMediaFileResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

    /**
     * Retrieve the test results for a given MediaFile
     * 	                (specified by url) within a given Job (specified
     * by
     * 	                jobName). If complete results are required, use the
     * 	                GetMediaFileStatus operation to ensure that the
     * 	                MediaFile has a status of "complete" before
     * 	                requesting the results.
     */
    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse getMediaFileResults_Deprecated_Dec09(com.tektronix.www.cerify.soap.client._20080509.GetMediaFileResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

    /**
     * Retrieve the test results for a given MediaFile
     * 	                (specified by url) within a given Job (specified
     * by
     * 	                jobName). If complete results are required, use the
     * 	                GetMediaFileStatus operation to ensure that the
     * 	                MediaFile has a status of "complete" before
     * 	                requesting the results.
     */
    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse getMediaFileResults_Deprecated_May10(com.tektronix.www.cerify.soap.client._20091202.GetMediaFileResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

    /**
     * Retrieve the test results for a given MediaFile
     * 	                (specified by url) within a given Job (specified
     * by
     * 	                jobName). If complete results are required, use the
     * 	                GetMediaFileStatus operation to ensure that the
     * 	                MediaFile has a status of "complete" before
     * 	                requesting the results.
     */
    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse getMediaFileResults_Deprecated_Dec10(com.tektronix.www.cerify.soap.client._20100523.GetMediaFileResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

    /**
     * Create a new MediaSet. The MediaFile specified by url is
     *                 added to the MediaSet. The URL must be within the
     *                 MediaLocation specified.
     */
    public com.tektronix.www.cerify.soap.client.CreateMediaSetResponse createMediaSet(com.tektronix.www.cerify.soap.client.CreateMediaSet request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault, com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.MediaSetNameInUseFault, com.tektronix.www.cerify.soap.client.URLNotAccessibleFault;

    /**
     * Create a new Job using an existing MediaSet and Profile.
     */
    public com.tektronix.www.cerify.soap.client.CreateJobResponse createJob(com.tektronix.www.cerify.soap.client.CreateJob request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.JobNameInUseFault, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault, com.tektronix.www.cerify.soap.client.ProfileDoesntExistFault;

    /**
     * Retrieve the status for a given MediaFile (specified by
     *                 url) within a given Job (specified by JobName).
     */
    public com.tektronix.www.cerify.soap.client.GetMediaFileStatusResponse getMediaFileStatus(com.tektronix.www.cerify.soap.client.GetMediaFileStatus request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

    /**
     * Retrieve the status for a given Job (specified by
     *                 jobName).
     */
    public com.tektronix.www.cerify.soap.client.GetJobStatusResponse getJobStatus(com.tektronix.www.cerify.soap.client.GetJobStatus request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault;

    /**
     * Retrieve the test results for a given Job (specified by
     *                 jobName). If a complete set of results is desired,
     * use
     *                 the GetJobStatus operation to ensure that the Job
     * has a
     *                 status of "complete" before requesting the
     *                 results.
     */
    public com.tektronix.www.cerify.soap.client.GetJobResultsResponse getJobResults(com.tektronix.www.cerify.soap.client._20101220.GetJobResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault;

    /**
     * Retrieve the test results for a given Job (specified by
     *                 jobName). If a complete set of results is desired,
     * use
     *                 the GetJobStatus operation to ensure that the Job
     * has a
     *                 status of "complete" before requesting the
     *                 results.
     */
    public com.tektronix.www.cerify.soap.client.GetJobResultsResponse getJobResults_Deprecated(com.tektronix.www.cerify.soap.client.GetJobResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault;

    /**
     * Retrieve the test results for a given Job (specified by
     * 	                jobName). If a complete set of results is desired,
     * use
     * 	                the GetJobStatus operation to ensure that the Job
     * has a
     * 	                status of "complete" before requesting the
     * 	                results.
     */
    public com.tektronix.www.cerify.soap.client.GetJobResultsResponse getJobResults_Deprecated_Dec09(com.tektronix.www.cerify.soap.client._20080509.GetJobResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault;

    /**
     * Retrieve the test results for a given Job (specified by
     * 	                jobName). If a complete set of results is desired,
     * use
     * 	                the GetJobStatus operation to ensure that the Job
     * has a
     * 	                status of "complete" before requesting the
     * 	                results.
     */
    public com.tektronix.www.cerify.soap.client.GetJobResultsResponse getJobResults_Deprecated_May10(com.tektronix.www.cerify.soap.client._20091202.GetJobResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault;

    /**
     * Retrieve the test results for a given Job (specified by
     *                 jobName). If a complete set of results is desired,
     * use
     *                 the GetJobStatus operation to ensure that the Job
     * has a
     *                 status of "complete" before requesting the
     *                 results.
     */
    public com.tektronix.www.cerify.soap.client.GetJobResultsResponse getJobResults_Deprecated_Dec10(com.tektronix.www.cerify.soap.client._20100523.GetJobResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault;

    /**
     * Retrieve the test results for a given Job (specified by
     * 	                jobName). If a complete set of results is desired,
     * use
     * 	                the GetJobStatus operation to ensure that the Job
     * has a
     * 	                status of "complete" before requesting the
     * 	                results.
     */
    public com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse getMediaResultsFromAllRuns(com.tektronix.www.cerify.soap.client._20101220.GetMediaResultsFromAllRuns request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

    /**
     * Retrieve the test results for a given Job (specified by
     * 	                jobName). If a complete set of results is desired,
     * use
     * 	                the GetJobStatus operation to ensure that the Job
     * has a
     * 	                status of "complete" before requesting the
     * 	                results.
     */
    public com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse getMediaResultsFromAllRuns_Deprecated_Dec10(com.tektronix.www.cerify.soap.client._20100523.GetMediaResultsFromAllRuns request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

    /**
     * Retrieve the test results for a given Job (specified by
     * 	                jobName). If a complete set of results is desired,
     * use
     * 	                the GetJobStatus operation to ensure that the Job
     * has a
     * 	                status of "complete" before requesting the
     * 	                results.
     */
    public com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse getMediaResultsFromAllRuns_Deprecated_May10(com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRuns request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

    /**
     * Add a new MediaFile (specified by url) to an existing
     *                 MediaSet (specified by mediaSetName). The URL must
     * be
     *                 within the MediaLocation specified.
     */
    public com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSetResponse addMediaFileToMediaSet(com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSet request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault, com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault, com.tektronix.www.cerify.soap.client.URLNotAccessibleFault;

    /**
     * Change the state or priority of an existing Job.
     */
    public com.tektronix.www.cerify.soap.client.ControlJobResponse controlJob(com.tektronix.www.cerify.soap.client.ControlJob request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.JobIsArchivedFault;

    /**
     * Change the state of a MediaSet. Currently supports
     *                 deletion only.
     */
    public com.tektronix.www.cerify.soap.client.ControlMediaSetResponse controlMediaSet(com.tektronix.www.cerify.soap.client.ControlMediaSet request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.MediaSetIsInUseFault, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault;

    /**
     * Retrieve a list of active (not archived) Job names
     *                 according to Date and/or Status criteria. If
     *                 createTimeRangeFrom is populated, then all active
     * Jobs
     *                 created on or after that date will be returned. If
     * createTimeRangeTo is populated, then all active Jobs
     *                 created on or before that date will be returned. If
     * status is populated, then all active Jobs having that
     *                 status will be returned. More than one criterion can
     * be
     *                 populated, in which case the results are 'ANDed'
     *                 together.
     */
    public com.tektronix.www.cerify.soap.client.GetJobsResponse getJobs(com.tektronix.www.cerify.soap.client.GetJobs request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.InvalidRangeFault;

    /**
     * Retrieve overall status information in order to make Job
     *                 assignment decisions. Returned values are: total Jobs
     * pending (this is the number of Jobs shown as
     *                 "Waiting" in the Web UI), number of MTUs in
     *                 cluster (if clustered, otherwise 1), and total file
     * capacity (file capacity per MTU multiplied by number of
     *                 MTUs).
     */
    public com.tektronix.www.cerify.soap.client.GetSystemStatusResponse getSystemStatus(com.tektronix.www.cerify.soap.client.GetSystemStatus request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault;

    /**
     * Retrieve a list of all active (non-archived) Profile
     *                 names.
     */
    public com.tektronix.www.cerify.soap.client.GetProfilesResponse getProfiles(com.tektronix.www.cerify.soap.client.GetProfiles request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault;

    /**
     * Retrieve a list of MediaLocations available in Cerify.
     */
    public com.tektronix.www.cerify.soap.client.GetMediaLocationsResponse getMediaLocations(com.tektronix.www.cerify.soap.client.GetMediaLocations request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault;
    public com.tektronix.www.cerify.soap.client.GetTemplatesForJobResponse getTemplatesForJob(com.tektronix.www.cerify.soap.client.GetTemplatesForJob request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault;
}
