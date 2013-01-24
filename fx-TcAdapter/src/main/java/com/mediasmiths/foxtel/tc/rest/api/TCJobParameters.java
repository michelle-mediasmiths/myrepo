package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class TCJobParameters
{
	public TCOutputPurpose purpose = TCOutputPurpose.TX_SD;
	public TCResolution sourceType = TCResolution.HD;
	public TCAudioType audioType = TCAudioType.STEREO;


	public String inputFile = "\\\\path\\to\\myfile.mxf";
	public String outputFolder = "\\\\path\\to\\output";
	public String outputFileBasename = "SomeVideo";

	public TCBugOptions bug = new TCBugOptions();
	public TCTimecodeOptions timecode = new TCTimecodeOptions();

	public int priority = 5;
}
