package com.mediasmiths.stdEvents.report.entity;

public class Export
{
	String dateRange;
	String title;
	String materialID;
	String channel;
	String taskStatus;
	String exportType;
	String duration;
	
	public Export()
	{
	}

	public Export(String title, String materialID)
	{
		super();
		this.title = title;
		this.materialID = materialID;
	}

	public Export(
			String dateRange,
			String title,
			String materialID,
			String channel,
			String taskStatus,
			String exportType,
			String duration)
	{
		super();
		this.dateRange = dateRange;
		this.title = title;
		this.materialID = materialID;
		this.channel = channel;
		this.taskStatus = taskStatus;
		this.exportType = exportType;
		this.duration = duration;
	}

	public String getDateRange()
	{
		return dateRange;
	}

	public void setDateRange(String dateRange)
	{
		this.dateRange = dateRange;
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

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public String getTaskStatus()
	{
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus)
	{
		this.taskStatus = taskStatus;
	}

	public String getExportType()
	{
		return exportType;
	}

	public void setExportType(String exportType)
	{
		this.exportType = exportType;
	}

	public String getDuration()
	{
		return duration;
	}

	public void setDuration(String duration)
	{
		this.duration = duration;
	}
	
	
}
