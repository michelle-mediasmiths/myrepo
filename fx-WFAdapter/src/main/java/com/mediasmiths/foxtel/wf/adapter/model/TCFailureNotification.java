package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TCFailureNotification
{
	private String packageID;
	
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
	//private String errorMessage;

	@XmlElement
	public String getPackageID()
	{
		return packageID;
	}
	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}
	
	/*@XmlElement
	public String getErrorMessage()
	{
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}*/


}
