package com.mediasmiths.stdEvents.events.db.entity.nagios;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MntReportEntity
{
	@XmlElement
	public String status;

	@XmlElement
	public String fileName;

	@XmlElement
	public String ipAddress;

	@XmlElement
	public String hostName;

	/**
	 * Getters and setters
	 */

	public String getstatus()
	{
		return status;
	}

	public void setstatus(String status)
	{
		this.status = status;
	}

	public String getfileName()
	{
		return fileName;
	}

	public void setfileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String ipAddress()
	{
		return ipAddress;
	}

	public void setipAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	public String hostName()
	{
		return hostName;
	}

	public void sethostName(String hostName)
	{
		this.hostName = hostName;
	}

}
