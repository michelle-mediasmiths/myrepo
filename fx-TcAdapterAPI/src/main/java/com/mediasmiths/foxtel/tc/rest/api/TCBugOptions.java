package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class TCBugOptions
{
	@XmlElement(required = true)
	public String channel;
	@XmlElement(required = true)
	public TCLocation position = TCLocation.BOTTOM_RIGHT;
	@XmlElement(required = true)
	public double opacity = 80D;
}
