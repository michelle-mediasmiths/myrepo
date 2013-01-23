package com.mediasmiths.foxtel.tc.service;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class CarbonJobParameters
{
	// HD or SD source
	public CarbonSourceType sourceType = CarbonSourceType.HD;
	// TX or DVD destination
	public CarbonDestinationType destinationType = CarbonDestinationType.TX;
	// is audio DolbyE?
	public CarbonInputAudioType audioType = CarbonInputAudioType.STEREO;


	public String inputFile = "\\\\path\\to\\myfile.mxf";
	public String outputFolder = "\\\\path\\to\\output";
	public String outputFileBasename = "SomeVideo";


	public CarbonBugOptions bug = new CarbonBugOptions();
	public CarbonTimecodeOptions timecode = new CarbonTimecodeOptions();
}
