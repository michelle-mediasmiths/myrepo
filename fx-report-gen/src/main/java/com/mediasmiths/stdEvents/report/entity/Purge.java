package com.mediasmiths.stdEvents.report.entity;

public class Purge
{
	String dateRange;
	String channel;
	String mediaID;
	String purgeStatus;
	String protectedStatus;
	String extendedStatus;
	String size;
	
	public Purge()
	{
	}
	
	public Purge(String channel, String mediaID)
	{
		super();
		this.channel = channel;
		this.mediaID = mediaID;
	}

	public Purge(
			String dateRange,
			String channel,
			String mediaID,
			String purgeStatus,
			String protectedStatus,
			String extendedStatus,
			String size)
	{
		super();
		this.dateRange = dateRange;
		this.channel = channel;
		this.mediaID = mediaID;
		this.purgeStatus = purgeStatus;
		this.protectedStatus = protectedStatus;
		this.extendedStatus = extendedStatus;
		this.size = size;
	}

	public String getDateRange()
	{
		return dateRange;
	}

	public void setDateRange(String dateRange)
	{
		this.dateRange = dateRange;
	}

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public String getMediaID()
	{
		return mediaID;
	}

	public void setMediaID(String mediaID)
	{
		this.mediaID = mediaID;
	}

	public String getPurgeStatus()
	{
		return purgeStatus;
	}

	public void setPurgeStatus(String purgeStatus)
	{
		this.purgeStatus = purgeStatus;
	}

	public String getProtectedStatus()
	{
		return protectedStatus;
	}

	public void setProtectedStatus(String protectedStatus)
	{
		this.protectedStatus = protectedStatus;
	}

	public String getExtendedStatus()
	{
		return extendedStatus;
	}

	public void setExtendedStatus(String extendedStatus)
	{
		this.extendedStatus = extendedStatus;
	}

	public String getSize()
	{
		return size;
	}

	public void setSize(String size)
	{
		this.size = size;
	}
}
