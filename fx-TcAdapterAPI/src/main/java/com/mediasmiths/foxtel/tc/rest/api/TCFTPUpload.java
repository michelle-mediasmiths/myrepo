package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class TCFTPUpload
{
	/**
	 * The base output filename to use (the transcoder will NOT append the outgoing file format extension to this String) it will need to be included 
	 */
	@XmlElement(required = true)
	public String filename;
	
	/**
	 * The folder to upload media to (relative to ftp users home location)
	 */
	@XmlElement(required = true)
	public String folder;

	@XmlElement(required = true)
	public String server;
	
	@XmlElement(required = true)
	public String user;
	
	@XmlElement(required = true)
	public String password;
}
