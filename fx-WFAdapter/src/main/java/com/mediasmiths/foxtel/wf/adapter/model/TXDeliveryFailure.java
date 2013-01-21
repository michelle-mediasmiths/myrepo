package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TXDeliveryFailure
{
	
	private String packageID;
	private String stage;
	
	@XmlElement
	public String getPackageID()
	{
		return packageID;
	}
	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}
	@XmlElement
	public String getStage()
	{
		return stage;
	}
	public void setStage(String stage)
	{
		this.stage = stage;
	}
	
	private long taskID;
	
	public long getTaskID()
	{
		return taskID;
	}

	@XmlElement(required = true)
	public void setTaskID(long taskID)
	{
		this.taskID = taskID;
	}

}
