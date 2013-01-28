package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "TCJob")
@XmlType
public class TCJobParameters
{
	@XmlElement
	public TCOutputPurpose purpose = TCOutputPurpose.TX_SD;
	@XmlElement
	public TCResolution resolution = TCResolution.HD;
	@XmlElement
	public TCAudioType audioType = TCAudioType.STEREO;

	/**
	 * The path to the input file, expressed as a unix filepath
	 */
	@XmlElement
	public String inputFile = "/path/to/myfile.mxf";

	/**
	 * The path to the output folder, expressed as a unix filepath
	 */
	@XmlElement
	public String outputFolder = "/path/to/output/";
	/**
	 * The base output filename to use (the transcoder will append the outgoing file format extension to this String)
	 */
	@XmlElement
	public String outputFileBasename = "SomeVideo";


	/**
	 * The bug to apply (if required)
	 */
	@XmlElement(required = false)
	public TCBugOptions bug = new TCBugOptions();

	/**
	 * The timecode display to apply (if required)
	 */
	@XmlElement(required = false)
	public TCTimecodeOptions timecode = new TCTimecodeOptions();

	/**
	 * The job priority (1-10)
	 */
	@XmlElement
	public int priority = 5;
}
