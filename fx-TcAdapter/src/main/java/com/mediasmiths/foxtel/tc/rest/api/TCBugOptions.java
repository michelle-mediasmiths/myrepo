package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class TCBugOptions
{
	@XmlElement
	public String channel = "AE";
	@XmlElement
	public TCLocation position = TCLocation.BOTTOM_LEFT;
	@XmlElement
	public double opacity = 80D;
}
