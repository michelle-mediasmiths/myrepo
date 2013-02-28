package com.mediasmiths.stdEvents.report.entity;

public class PurgeRT
{
	String dateRange;
	String entityType;
	String title;
	String materialID;
	String channels;
	String isProtected;
	String extended;
	String purged;
	String expires;
	
	public PurgeRT()
	{
	}

	public PurgeRT(String entityType, String title)
	{
		super();
		this.entityType = entityType;
		this.title = title;
	}

	public PurgeRT(
			String dateRange,
			String entityType,
			String title,
			String materialID,
			String channels,
			String isProtected,
			String extended,
			String purged,
			String expores)
	{
		super();
		this.dateRange = dateRange;
		this.entityType = entityType;
		this.title = title;
		this.materialID = materialID;
		this.channels = channels;
		this.isProtected = isProtected;
		this.extended = extended;
		this.purged = purged;
		this.expires = expores;
	}

	public String getDateRange()
	{
		return dateRange;
	}

	public void setDateRange(String dateRange)
	{
		this.dateRange = dateRange;
	}

	public String getEntityType()
	{
		return entityType;
	}

	public void setEntityType(String entityType)
	{
		this.entityType = entityType;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getMaterialID()
	{
		return materialID;
	}

	public void setMaterialID(String materialID)
	{
		this.materialID = materialID;
	}

	public String getChannels()
	{
		return channels;
	}

	public void setChannels(String channels)
	{
		this.channels = channels;
	}

	public String getIsProtected()
	{
		return isProtected;
	}

	public void setIsProtected(String isProtected)
	{
		this.isProtected = isProtected;
	}

	public String getExtended()
	{
		return extended;
	}

	public void setExtended(String extended)
	{
		this.extended = extended;
	}

	public String getPurged()
	{
		return purged;
	}

	public void setPurged(String purged)
	{
		this.purged = purged;
	}

	public String getExpires()
	{
		return expires;
	}

	public void setExpires(String expires)
	{
		this.expires = expires;
	}
}
