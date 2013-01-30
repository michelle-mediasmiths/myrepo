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

	@XmlElement(required = true)
	public TCJobProgress state;

	@XmlElement(required = true)
	public String carbonState;

	@XmlElement(required = false)
	public String errorDetail;

	@XmlElement(required = false)
	public Integer priority;
}
