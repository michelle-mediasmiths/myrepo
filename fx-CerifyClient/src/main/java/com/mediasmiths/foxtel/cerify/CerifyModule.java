package com.mediasmiths.foxtel.cerify;

import static com.mediasmiths.foxtel.cerify.CerifyClientConfig.CERIFY_LOCATION_NAME;
import static com.mediasmiths.foxtel.cerify.CerifyClientConfig.CERIFY_LOCATION_URL;
import static com.mediasmiths.foxtel.cerify.CerifyClientConfig.CERITALK_SERVER;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.tektronix.www.cerify.soap.client.BaseCeritalkFault;
import com.tektronix.www.cerify.soap.client.CeriTalk_PortType;
import com.tektronix.www.cerify.soap.client.CeriTalk_ServiceLocator;
import com.tektronix.www.cerify.soap.client.GetMediaLocations;
import com.tektronix.www.cerify.soap.client.GetMediaLocationsResponse;
import com.tektronix.www.cerify.soap.client.GetMediaLocationsResponseMedialocation;

public class CerifyModule extends AbstractModule {

	private final static Logger log = Logger.getLogger(CerifyModule.class);
	
	@Override
	protected void configure() {

	}

	@Provides
	public CeriTalk_PortType getServiceLocator(
			@Named(CERITALK_SERVER) String server) throws ServiceException {

		CeriTalk_ServiceLocator serviceLocator = new CeriTalk_ServiceLocator();
		serviceLocator.setCeriTalkSOAPEndpointAddress("http://" + server
				+ "/CeriTalk");

		return serviceLocator.getCeriTalkSOAP();
	}

	@Provides @Named(CERIFY_LOCATION_URL)
	private URI uriForMediaLocation(
			@Named(CERIFY_LOCATION_NAME) String locationName,
			CeriTalk_PortType service) throws BaseCeritalkFault,
			RemoteException, MalformedURIException {

		GetMediaLocations request = new GetMediaLocations();
		GetMediaLocationsResponse mediaLocations = service
				.getMediaLocations(request);
		GetMediaLocationsResponseMedialocation[] locations = mediaLocations
				.getMedialocation();

		URI locationURI = null;

		for (GetMediaLocationsResponseMedialocation location : locations) {
			if (location.getName().equals(locationName)) {
				locationURI = location.getUrl();
				break; // no need to keep looking now we have found our uri
			}
		}

		if (locationURI == null) {
			throw new RemoteException("Could not get url for location "
					+ locationName);
		} else {
			/*
			 * if a uri is returned by cerify of the form:
			 * file://c:/path/to/folder then "c" is interpreted as the host of
			 * the uri and the colon is stripped leaving uris that look like
			 * this : file://c/path/to/folder so instead we need to make the
			 * host of the uri the empty string and prefix the first slash of the new path
			 * with a colon giving the desired: file:///c:/path/to/folder
			 */

			if (locationURI.getHost().length() == 1
					&& !locationURI.getHost().equals("/")) {
				String newPath = locationURI.getHost() + ':'
						+ locationURI.getPath();
				String newHost = "";

				locationURI.setHost(newHost);
				locationURI.setPath(newPath);
			}
			
			log.info("location URI is "+ locationURI.toString());
			
			return locationURI;
		}
	}

}
