package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class TCTimecodeOptions
{
	public TCLocation location = TCLocation.BOTTOM;
	public TCTimecodeSize size = TCTimecodeSize.LARGE;
	public TCTimecodeColour background = TCTimecodeColour.TRANSPARENT;
	public TCTimecodeColour foreground = TCTimecodeColour.BLACK;
}
