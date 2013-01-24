package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class TCJobInfo
{
	@XmlAttribute
	public String id;

	@XmlElement(required = false)
	public TCJobResult result;

	@XmlElement(required = false)
	public String errorDetail;
}
