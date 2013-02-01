package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TXDeliveryFinished
{

	private String packageID;

	private long taskID;

	@XmlElement(required = true)
	public String getPackageID()
	{
		return packageID;
	}

	public long getTaskID()
	{
		return taskID;
	}

	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}

	@XmlElement(required = true)
	public void setTaskID(long taskID)
	{
		this.taskID = taskID;
	}

}
