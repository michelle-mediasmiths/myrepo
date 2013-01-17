package com.mediasmiths.foxtel.cerify.medialocation;

import static com.mediasmiths.foxtel.cerify.CerifyClientConfig.CERIFY_LOCATIONS_NAMES;
import static com.mediasmiths.foxtel.cerify.CerifyClientConfig.CERIFY_LOCATIONS_PATHS;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.tektronix.www.cerify.soap.client.BaseCeritalkFault;
import com.tektronix.www.cerify.soap.client.CeriTalk_PortType;
import com.tektronix.www.cerify.soap.client.GetMediaLocations;
import com.tektronix.www.cerify.soap.client.GetMediaLocationsResponse;
import com.tektronix.www.cerify.soap.client.GetMediaLocationsResponseMedialocation;

@Singleton
public class MediaLocationService extends HashMap<String,MediaLocation>
{
	
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(MediaLocationService.class);
	
	private final CeriTalk_PortType service;
	
		@Inject
		public MediaLocationService(CeriTalk_PortType service, @Named(CERIFY_LOCATIONS_NAMES) String locations,@Named(CERIFY_LOCATIONS_PATHS) String paths) throws BaseCeritalkFault, RemoteException, MalformedURIException{
			super();
			
			this.service=service;
			
			List<String> locationNames = Arrays.asList(locations.split(","));
			List<String> locationPaths = Arrays.asList(paths.split(","));
			
			if(locationNames.size()!= locationPaths.size()){
				throw new IllegalArgumentException("Number of cerify media location names does no match the number of media location paths");
			}
			
			for (int i = 0; i < locationNames.size(); i++)
			{
				String name = locationNames.get(i);
				String path = locationPaths.get(i);
				URI uri = uriForMediaLocation(name);
				log.debug(String.format("Medialocation {%s} {%s} {%s}", name ,uri.toString(), path));
				MediaLocation ml = new MediaLocation(name, uri, path);
				this.put(name, ml);
			}
			
		}
		
		public MediaLocation getLocation(String locationName) throws MediaLocationNotFoundException{
			MediaLocation mediaLocation = this.get(locationName);
			if(mediaLocation == null){
				throw new MediaLocationNotFoundException(locationName);
			}
			return mediaLocation;
		}
	
		private URI uriForMediaLocation(
				String locationName) throws BaseCeritalkFault,
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

	public MediaLocation getLocationForAbsoluteFilePath(String file) throws MediaLocationNotFoundException
	{
		for (MediaLocation ml : this.values())
		{
			if (file.startsWith(ml.getPath()))
			{
				log.debug("returning medialocation " + ml.getName() + " for path" + file);
				return ml;
			}
		}
		throw new MediaLocationNotFoundException("could not determine media location for file " + file);
	}
}
