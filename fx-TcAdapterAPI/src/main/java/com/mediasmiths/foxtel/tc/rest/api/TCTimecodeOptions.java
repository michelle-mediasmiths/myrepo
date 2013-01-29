package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class TCTimecodeOptions
{
	@XmlElement(required = true)
	public TCLocation location = TCLocation.BOTTOM;
	@XmlElement(required = true)
	public TCTimecodeSize size = TCTimecodeSize.LARGE;
	@XmlElement(required = true)
	public TCTimecodeColour background = TCTimecodeColour.BLACK;
	@XmlElement(required = true)
	public TCTimecodeColour foreground = TCTimecodeColour.WHITE;
}
