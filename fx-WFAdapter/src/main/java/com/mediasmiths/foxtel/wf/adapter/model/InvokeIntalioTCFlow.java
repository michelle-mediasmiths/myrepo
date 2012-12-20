package com.mediasmiths.foxtel.wf.adapter.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InvokeIntalioTCFlow
{
	private String assetID;
	private String outputFolder;
	private boolean isForTX;
	private Date requiredDate;
	
	
	@XmlElement(required = true)
	public String getOutputFolder()
	{
		return outputFolder;
	}
	public void setOutputFolder(String outputFolder)
	{
		this.outputFolder = outputFolder;
	}

	@XmlElement(required = true)
	public String getAssetID()
	{
		return assetID;
	}
	public void setAssetID(String assetID)
	{
		this.assetID = assetID;
	}
	
	@XmlElement(required = true)
	public boolean isForTX()
	{
		return isForTX;
	}
	public void setForTX(boolean isForTX)
	{
		this.isForTX = isForTX;
	}
	
	@XmlElement(required = false)
	public Date getRequiredDate()
	{
		return requiredDate;
	}
	
	public void setRequiredDate(Date requiredDate)
	{
		this.requiredDate = requiredDate;
	}
	
}
