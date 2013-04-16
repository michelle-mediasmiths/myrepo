package com.mediasmiths.foxtel.wf.adapter.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetPriorityRequest
{
	@XmlElement(required=true)
	private String jobType;
	@XmlElement(required=true)
	private Date txDate;
	@XmlElement(required=true)
	private Date created;
	@XmlElement(required=true)
	private Integer currentPriority;
	
	public String getJobType()
	{
		return jobType;
	}
	public void setJobType(String jobType)
	{
		this.jobType = jobType;
	}
	public Date getTxDate()
	{
		return txDate;
	}
	public void setTxDate(Date txDate)
	{
		this.txDate = txDate;
	}
	public Date getCreated()
	{
		return created;
	}
	public void setCreated(Date created)
	{
		this.created = created;
	}
	public Integer getCurrentPriority()
	{
		return currentPriority;
	}
	public void setCurrentPriority(Integer currentPriority)
	{
		this.currentPriority = currentPriority;
	}
}
