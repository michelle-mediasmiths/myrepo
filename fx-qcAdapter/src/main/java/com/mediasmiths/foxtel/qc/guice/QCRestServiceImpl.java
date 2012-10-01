package com.mediasmiths.foxtel.qc.guice;

import java.io.File;
import java.rmi.RemoteException;
import java.security.Provider.Service;
import java.util.List;

import javassist.NotFoundException;

import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.InternalServerErrorException;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.cerify.CerifyClient;
import com.mediasmiths.foxtel.qc.QCJobIdentifier;
import com.mediasmiths.foxtel.qc.QCMediaResult;
import com.mediasmiths.foxtel.qc.QCRestService;
import com.mediasmiths.foxtel.qc.QCJobResult;
import com.mediasmiths.foxtel.qc.QCStartResponse;
import com.mediasmiths.foxtel.qc.QCStartStatus;
import com.mediasmiths.foxtel.qc.QCJobStatus;
import com.tektronix.www.cerify.soap.client.BaseCeritalkFault;
import com.tektronix.www.cerify.soap.client.GetJobResultsResponse;
import com.tektronix.www.cerify.soap.client.GetJobStatusResponse;
import com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse;
import com.tektronix.www.cerify.soap.client.JobDoesntExistFault;
import com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault;

public class QCRestServiceImpl implements QCRestService {

	private final static Logger log = Logger.getLogger(QCRestServiceImpl.class);

	private final CerifyClient cerifyClient;

	@Inject
	public QCRestServiceImpl(CerifyClient cerifyClient) {
		log.trace("Constructed");
		this.cerifyClient = cerifyClient;
	}

	@Override
	public QCJobStatus jobStatus(QCJobIdentifier job) throws NotFoundException {

		log.debug(String.format("Status request for job %s",
				job.getIdentifier()));

		try {
			GetJobStatusResponse status = cerifyClient.getStatus(job
					.getIdentifier());
			log.debug(String.format("Status of job %s is %s progress is %d ",
					job.getIdentifier(), status.getStatus().getValue(), status
							.getProgress().floatValue()));
			return new QCJobStatus(job, status.getStatus().getValue(),
					status.getProgress());
		} catch (JobDoesntExistFault e) {
			String message = String.format("Status request for job %s failed",
					job.getIdentifier());
			log.warn(message, e);
			throw new NotFoundException(message, e);
		} catch (RemoteException e) {
			log.error(String.format(
					"RemoteException querying status of job %s",
					job.getIdentifier()), e);
			throw new InternalServerErrorException(e);
		}
	}

	@Override
	public String ping() {
		return "ping";
	}

	@Override
	public QCStartResponse start(String file, String ident, String profileName) {

		log.debug(String.format("Start requested for file  %s", file));

		try {
			String jobName = cerifyClient.startQcForFile(file, ident,
					profileName);
			log.info(String.format("Job %s created", jobName));
			QCStartResponse res = new QCStartResponse(QCStartStatus.STARTED);
			res.setQcIdentifier(new QCJobIdentifier(jobName));
			return res;
		} catch (Exception e) {
			log.error("Error starting qc", e);
			return new QCStartResponse(QCStartStatus.ERROR);
		}

	}

	@Override
	public QCJobResult jobResult(QCJobIdentifier ident)
			throws NotFoundException {

		log.debug(String.format("Result requested for job %s",
				ident.getIdentifier()));
		try {
			GetJobResultsResponse jobResults = cerifyClient.getJobResult(ident
					.getIdentifier());

			QCJobResult qcr = new QCJobResult();
			qcr.setIdent(new QCJobIdentifier(jobResults.getName(), jobResults
					.getProfile()));
			qcr.setResult(jobResults.getResult().getValue());
			qcr.setCompleted(jobResults.getCompleted());
			qcr.setUrl(jobResults.getUrl());

			log.debug("Returning job result: " + qcr.toString());
			return qcr;

		} catch (JobDoesntExistFault e) {
			String message = String.format("Result request for job %s failed",
					ident.getIdentifier());
			log.warn(message, e);
			throw new NotFoundException(message, e);
		} catch (RemoteException e) {
			log.error("remote exception querying job result for job " + ident,
					e);
			throw new InternalServerErrorException(e);
		}

	}

	@Override
	public Boolean profileExists(String profileName) {

		log.debug(String.format("Profile Exists request for %s", profileName));
		List<String> listProfiles;
		try {
			listProfiles = cerifyClient.listProfiles();
			if (listProfiles.contains(profileName)) {
				log.debug(String.format("Profile %s exists", profileName));
				return Boolean.valueOf(true);
			} else {
				log.debug(String.format("Profile %s does not exist",
						profileName));
				return Boolean.valueOf(false);
			}

		} catch (RemoteException e) {
			log.error("remote exception querying profile existance ", e);
			throw new InternalServerErrorException(e);
		}
	}

	@Override
	public QCMediaResult mediaResult(String file, QCJobIdentifier ident,
			Integer runNumber) throws NotFoundException {

		log.debug(String.format(
				"Media result for file %s and job %s requested", file,
				ident.getIdentifier()));

		try {
			GetMediaFileResultsResponse mediaResult = cerifyClient.getMediaResult(file, ident.getIdentifier(),
					runNumber.intValue());
			//TODO finish this!
			return null;
			
		} catch (JobDoesntExistFault e) {
			String message = String.format(
					"Media Result request for job %s failed",
					ident.getIdentifier());
			log.warn(message, e);
			throw new NotFoundException(message, e);
		} catch (MediaFileNotInJobFault e) {
			String message = String.format("Media %s is not part of job %s ",
					file, ident.getIdentifier());
			log.warn(message, e);
			throw new NotFoundException(message, e);
		} catch (RemoteException e) {
			log.error("remote exception requesting media qc result ", e);
			throw new InternalServerErrorException(e);
		} catch (MalformedURIException e) {
			log.error("The cerify client has produced a malformed uri", e);
			throw new InternalServerErrorException(e);
		}
	}
}
