package com.mediasmiths.foxtel.tc.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TCBuildJobResponse
{
	private String pcpXML;
	
	@XmlElement(name = "pcp", required = true)
	public String getPcpXML()
	{
		return pcpXML;
	}
	public void setPcpXML(String pcpXML)
	{
		this.pcpXML = pcpXML;
	}
	@XmlElement(name = "priority", required = true)
	public Integer getPriority()
	{
		return priority;
	}
	public void setPriority(Integer priority)
	{
		this.priority = priority;
	}
	private Integer priority;
}
