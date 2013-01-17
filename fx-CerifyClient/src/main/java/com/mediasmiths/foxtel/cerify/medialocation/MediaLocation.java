package com.mediasmiths.foxtel.cerify.medialocation;

import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;

public class MediaLocation
{
	private String name;
	private URI uri;
	private String path;
	
	private final static Logger log = Logger.getLogger(MediaLocation.class);
	
	public MediaLocation(String name, URI uri, String path){
		this.name = name;
		this.uri = uri;
		this.path = path;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public URI getUri()
	{
		return uri;
	}
	public void setUri(URI uri)
	{
		this.uri = uri;
	}
	public String getPath()
	{
		return path;
	}
	public void setPath(String path)
	{
		this.path = path;
	}
	
	public URI resolveUriForFile(String filePath) throws MalformedURIException
	{

		log.debug("Resolving uri for " + filePath);

		// filepath is relative to the media location
		URI resolved = new URI(getUri());
		resolved.appendPath(filePath);
		log.debug("Resolved uri for " + filePath + " as " + resolved.toString());

		return resolved;
	}
}
