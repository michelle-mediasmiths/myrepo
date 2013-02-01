package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;

public abstract class TCNotification
{
	private String assetID;

	private long taskID;
	
	private boolean isForTXDelivery;

	public boolean isForTXDelivery()
	{
		return isForTXDelivery;
	}

	@XmlElement(required = true)
	public void setForTXDelivery(boolean isForTXDelivery)
	{
		this.isForTXDelivery = isForTXDelivery;
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

	@XmlElement
	public String getAssetID()
	{
		return assetID;
	}

	public void setAssetID(String assetID)
	{
		this.assetID = assetID;
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

}
