package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement(name = "JobInfo")
public class TCJobInfo
{
	@XmlElement(required = true)
	public String id;

	@XmlElement(required = false)
	public TCJobResult result;

	@XmlElement(required = false)
	public String errorDetail;
}
