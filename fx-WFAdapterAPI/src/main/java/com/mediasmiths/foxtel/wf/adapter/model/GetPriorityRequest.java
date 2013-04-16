package com.mediasmiths.foxtel.wf.adapter.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetPriorityRequest
{

	private String jobType;
	
	private Date txDate;

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
	@XmlElement
	public Date getTxDate()
	{
		return txDate;
	}
	public void setTxDate(Date txDate)
	{
		this.txDate = txDate;
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
