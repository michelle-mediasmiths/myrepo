package com.mediasmiths.foxtel.tc.service;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class CarbonTimecodeOptions
{
	public CarbonLocation location = CarbonLocation.BOTTOM;
	public CarbonTimecodeSize size = CarbonTimecodeSize.LARGE;
	public CarbonColour background = CarbonColour.TRANSPARENT;
	public CarbonColour foreground = CarbonColour.BLACK;
}
