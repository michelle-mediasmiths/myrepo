package com.mediasmiths.foxtel.wf.adapter.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;

@XmlRootElement
public class InvokeExport
{
	private String assetID;
	private Date requiredDate;
	private Date created;
	private long taskID;
	private TCJobParameters tcParams;
	private String jobType;
	


	public TCJobParameters getTcParams()
	{
		return tcParams;
	}

	@XmlElement(required = true)
	public void setTcParams(TCJobParameters tcParams)
	{
		this.tcParams = tcParams;
	}

	public long getTaskID()
	{
		return taskID;
	}

	@XmlElement(required = true)
	public void setTaskID(long taskID)
	{
		this.taskID = taskID;
	}

	@XmlElement(required = true)
	public String getAssetID()
	{
		return assetID;
	}
	public void setAssetID(String assetID)
	{
		this.assetID = assetID;
	}
	
	@XmlElement(required = false)
	public Date getRequiredDate()
	{
		return requiredDate;
	}
	
	public void setRequiredDate(Date requiredDate)
	{
		this.requiredDate = requiredDate;
	}
	
	private String title;

	@XmlElement(required=true)
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@XmlElement
	public String getJobType()
	{
		return jobType;
	}

	public void setJobType(String jobType)
	{
		this.jobType = jobType;
	}
	
	@XmlElement
	public Date getCreated()
	{
		return created;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}
	
}
