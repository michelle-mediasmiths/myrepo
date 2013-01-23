package com.mediasmiths.foxtel.tc.service;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class CarbonBugOptions
{
	@XmlElement
	public String channel = "AE";
	@XmlElement
	public CarbonLocation position = CarbonLocation.BOTTOM_LEFT;
	@XmlElement
	public double opacity = 80D;
}
