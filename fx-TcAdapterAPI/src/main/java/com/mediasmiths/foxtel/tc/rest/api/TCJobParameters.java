package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "TCJob")
@XmlType
public class TCJobParameters
{
	@XmlElement(required = true)
	public TCOutputPurpose purpose;
	@XmlElement(required = true)
	public TCResolution resolution;
	@XmlElement(required = true)
	public TCAudioType audioType;

	/**
	 * The path to the input file, expressed as a unix filepath
	 */
	@XmlElement(required = true)
	public String inputFile;

	/**
	 * The path to the output folder, expressed as a unix filepath
	 */
	@XmlElement(required = true)
	public String outputFolder;
	/**
	 * The base output filename to use (the transcoder will append the outgoing file format extension to this String)
	 */
	@XmlElement(required = true)
	public String outputFileBasename;


	/**
	 * The bug to apply (if required)
	 */
	@XmlElement(required = false)
	public TCBugOptions bug;

	/**
	 * The timecode display to apply (if required)
	 */
	@XmlElement(required = false)
	public TCTimecodeOptions timecode;

	/**
	 * The job priority (1-10)
	 */
	@XmlElement(required = true)
	public int priority = 5;
}
