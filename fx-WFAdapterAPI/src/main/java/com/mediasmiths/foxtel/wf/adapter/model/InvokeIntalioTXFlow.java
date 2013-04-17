package com.mediasmiths.foxtel.wf.adapter.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;

@XmlRootElement
public class InvokeIntalioTXFlow
{
	private boolean isAO;
	
	private String packageID;

	private Date requiredDate;

	private long taskID;

	private TCJobParameters tcParams;

	private String title;
	
	private Date created;

	@XmlElement(required = true)
	public String getPackageID()
	{
		return packageID;
	}

	@XmlElement(required = true)
	public Date getRequiredDate()
	{
		return requiredDate;
	}

	@XmlElement(required = true)
	public long getTaskID()
	{
		return taskID;
	}
	
	@XmlElement(required = true)
	public TCJobParameters getTcParams()
	{
		return tcParams;
	}
	@XmlElement(required = true)
	public String getTitle()
	{
		return title;
	}

	public boolean isAO()
	{
		return isAO;
	}

	public void setAO(boolean isAO)
	{
		this.isAO = isAO;
	}

	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}

	public void setRequiredDate(Date requiredDate)
	{
		this.requiredDate = requiredDate;
	}

	public void setTaskID(long taskID)
	{
		this.taskID = taskID;
	}

	public void setTcParams(TCJobParameters tcParams)
	{
		this.tcParams = tcParams;
	}

	public void setTitle(String title)
	{
		this.title = title;
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
