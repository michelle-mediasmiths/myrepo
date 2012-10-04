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
	private ProfileList profileList;

	public ProfileList getProfileList()
	{
		return profileList;
	}

	@XmlElement(name = "ProfileList")
	public void setProfileList(ProfileList profileList)
	{
		this.profileList = profileList;
	}

	public String getError()
	{
		return error;
	}

	public String getSuccess()
	{
		return success;
	}

	public JobList getJobList()
	{
		return jobList;
	}

	@XmlElement(name = "JobList")
	public void setJobList(JobList jobList)
	{
		this.jobList = jobList;
	}

	@XmlAttribute(name = "Success")
	public void setSuccess(String success)
	{
		this.success = success;
	}

	@XmlAttribute(name = "Error")
	public void setError(String error)
	{
		this.error = error;
	}
}
