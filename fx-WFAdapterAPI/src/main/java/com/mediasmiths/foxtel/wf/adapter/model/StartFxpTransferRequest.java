package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StartFxpTransferRequest
{
	private Long taskID;
	private String packageID;
	
	@XmlElement(required = true)
	public Long getTaskID()
	{
		return taskID;
	}
	public void setTaskID(Long taskID)
	{
		this.taskID = taskID;
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
}
