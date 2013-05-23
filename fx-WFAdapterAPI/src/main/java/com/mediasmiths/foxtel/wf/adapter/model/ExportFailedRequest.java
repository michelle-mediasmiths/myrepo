package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExportFailedRequest
{

	private Long taskID;

	@XmlElement(required=true)
	public Long getTaskID()
	{
		return taskID;
	}

	public void setTaskID(Long taskID)
	{
		this.taskID = taskID;
	}
	
	private String failureReason;

	@XmlElement(required=true)
	public String getFailureReason()
	{
		return failureReason;
	}

	public void setFailureReason(String failureReason)
	{
		this.failureReason = failureReason;
	}
	
}
