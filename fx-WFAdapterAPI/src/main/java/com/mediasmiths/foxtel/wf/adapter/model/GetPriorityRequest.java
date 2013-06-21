package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
public class GetPriorityRequest
{

	private String jobType;
	
	private String packageID;

	private Date created;

	private Integer currentPriority;
	
	@XmlElement(required = true)
	public String getJobType()
	{
		return jobType;
	}
	public void setJobType(String jobType)
	{
		this.jobType = jobType;
	}
	@XmlElement(required = true)
	public String getPackageID()
	{
		return packageID;
	}
	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}
	@XmlElement(required=true)
	public Date getCreated()
	{
		return created;
	}
	public void setCreated(Date created)
	{
		this.created = created;
	}
	@XmlElement(required=true)
	public Integer getCurrentPriority()
	{
		return currentPriority;
	}
	public void setCurrentPriority(Integer currentPriority)
	{
		this.currentPriority = currentPriority;
	}
}
