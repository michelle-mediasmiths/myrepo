package com.mediasmiths.foxtel.carbon.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Reply")
public class CarbonReply
{

	private String success;
	private String error;

	private JobList jobList;

	@XmlAttribute(name = "Error")
	public String getError()
	{
		return error;
	}

	@XmlAttribute(name = "Success")
	public String getSuccess()
	{
		return success;
	}

	@XmlElement(name = "JobList")
	public JobList getJobList()
	{
		return jobList;
	}

	public void setJobList(JobList jobList)
	{
		this.jobList = jobList;
	}

	public void setSuccess(String success)
	{
		this.success = success;
	}

	public void setError(String error)
	{
		this.error = error;
	}
}
