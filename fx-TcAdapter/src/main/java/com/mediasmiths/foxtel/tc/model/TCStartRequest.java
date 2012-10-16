package com.mediasmiths.foxtel.tc.model;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TCStartRequest
{
	@XmlElement(name="jobName")
	public String getJobName()
	{
		return jobName;
	}
	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}
	
	String jobName;	
	String pcpXml;
	
	@XmlElement(name="jobXml")
	public String getPcpXml()
	{
		return pcpXml;
	}
	public void setPcpXml(String pcpXml)
	{
		this.pcpXml = pcpXml;
	}

}
