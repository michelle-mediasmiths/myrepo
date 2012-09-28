package com.mediasmiths.foxtel.cerify;

import java.io.File;
import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tektronix.www.cerify.soap.client.BaseCeritalkFault;
import com.tektronix.www.cerify.soap.client.CeriTalk_PortType;
import com.tektronix.www.cerify.soap.client.CreateJob;
import com.tektronix.www.cerify.soap.client.CreateMediaSet;
import com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault;
import com.tektronix.www.cerify.soap.client.MediaSetNameInUseFault;
import com.tektronix.www.cerify.soap.client.PriorityType;
import com.tektronix.www.cerify.soap.client.URLNotAccessibleFault;
import com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault;

import static com.mediasmiths.foxtel.cerify.CerifyClientConfig.CERIFY_LOCATION_NAME;

public class CerifyClient {

	private final static Logger log = Logger.getLogger(CerifyClient.class);

	private final CeriTalk_PortType service;

	private final String mediaLocationName;

	@Inject
	public CerifyClient(CeriTalk_PortType service,
			@Named(CERIFY_LOCATION_NAME) String locationName) {
		this.service = service;
		this.mediaLocationName = locationName;
	}

	/**
	 * Starts QC for a given file, using the provided identifier as a basis for
	 * the names of the created MediaSet and Job
	 * 
	 * @param f
	 * @param ident
	 * @return
	 * @throws MalformedURIException
	 * @throws RemoteException
	 */
	public String startQcForFile(String file, String ident, String profileName) throws MalformedURIException,
			RemoteException {
		log.trace("QC start request for " + file);
		String mediaSetName = FilenameUtils.getBaseName(file)
				+ "_" + ident;
		createMediaSet(file, ident, mediaSetName);

		String jobName = mediaSetName + "_" + profileName;
		PriorityType priority = PriorityType.Medium;
		
		log.info(String.format("Creating a job %s", jobName));
		CreateJob createJob = new CreateJob(jobName, mediaSetName, profileName,
				priority);
		log.debug("Sending CreateJobRequest");
		service.createJob(createJob);

		return jobName;

	}

	private void createMediaSet(final String file, final String ident,
			final String mediaSetName) throws MalformedURIException,
			URLNotInMediaLocationFault, MediaLocationDoesntExistFault,
			MediaSetNameInUseFault, URLNotAccessibleFault, BaseCeritalkFault,
			RemoteException {

		log.info(String.format("Creating a mediaset %s", mediaSetName));

		URI media = resolveUriForFile(file);
		CreateMediaSet cms = new CreateMediaSet(mediaSetName,
				mediaLocationName, media);

		log.debug("Sending CreateMediaSetRequest");
		try {
			service.createMediaSet(cms);
		} catch (URLNotInMediaLocationFault e) {
			log.error("Error creating mediaset", e);
			throw e;
		} catch (MediaLocationDoesntExistFault e) {
			log.error("Error creating mediaset", e);
			throw e;
		} catch (MediaSetNameInUseFault e) {
			log.error("Error creating mediaset", e);
			throw e;
		} catch (URLNotAccessibleFault e) {
			log.error("Error creating mediaset", e);
			throw e;
		} catch (BaseCeritalkFault e) {
			log.error("Error creating mediaset", e);
			throw e;
		} catch (RemoteException e) {
			log.error("Error creating mediaset", e);
			throw e;
		}
	}

	private URI resolveUriForFile(String filePath) throws MalformedURIException {

		log.debug("Resolving uri for "+ filePath);

		// lets assume for now that the paths will be the same on both systems
		//TODO : find out how to identify the correct paths for files
		return new URI(new File(filePath).toURI().toString());
	}
	
	/**
	 * Call to validate configuration
	 * @return
	 */
	public boolean validateConfig(){
		//TODO : implement
		return true;
	}

}
