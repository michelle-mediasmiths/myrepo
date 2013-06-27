package com.mediasmiths.foxtel.wf.adapter.model;

import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
public class InvokeIntalioTXFlow
{
	private boolean isAO;
	
	private String packageID;

	private Date requiredDate;

	private long taskID;

	private TCJobParameters tcParams;

	private String title;
	
	private Date created;

	private String quarantineLocation;

	private String deliveryLocation;

	@XmlElement(required = true)
	public String getPackageID()
	{
		return packageID;
	}

	@XmlElement(required = true)
	public Date getRequiredDate()
	{
		return requiredDate;
	}

	@XmlElement(required = true)
	public long getTaskID()
	{
		return taskID;
	}
	
	@XmlElement(required = true)
	public TCJobParameters getTcParams()
	{
		return tcParams;
	}
	@XmlElement(required = true)
	public String getTitle()
	{
		return title;
	}

	public boolean isAO()
	{
		return isAO;
	}

	public void setAO(boolean isAO)
	{
		this.isAO = isAO;
	}

	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}

	public void setRequiredDate(Date requiredDate)
	{
		this.requiredDate = requiredDate;
	}

	public void setTaskID(long taskID)
	{
		this.taskID = taskID;
	}

	public void setTcParams(TCJobParameters tcParams)
	{
		this.tcParams = tcParams;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}
	
	@XmlElement
	public Date getCreated()
	{
		return created;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	@XmlElement(required = true)
	public String getQuarantineLocation()
	{
		return quarantineLocation;
	}


	public void setQuarantineLocation(final String quarantineLocation)
	{
		this.quarantineLocation = quarantineLocation;
	}

	@XmlElement(required = true)
	public String getDeliveryLocation()
	{
		return deliveryLocation;
	}


	public void setDeliveryLocation(final String deliveryLocation)
	{
		this.deliveryLocation = deliveryLocation;
	}
}
