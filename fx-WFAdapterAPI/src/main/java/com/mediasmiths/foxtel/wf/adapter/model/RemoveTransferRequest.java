package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RemoveTransferRequest
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
}
