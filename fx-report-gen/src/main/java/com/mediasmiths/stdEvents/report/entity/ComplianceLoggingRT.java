package com.mediasmiths.stdEvents.report.entity;

public class ComplianceLoggingRT
{
	String dateRange;
	String title;
	String materialID;
	String channel;
	String taskStatus;
	String taskStart;
	String taskFinish;
	String externalCompliance;
	
	public ComplianceLoggingRT()
	{
	}
	
	public ComplianceLoggingRT(String title, String materialID)
	{
		super();
		this.title = title;
		this.materialID = materialID;
	}

	public ComplianceLoggingRT(
			String dateRange,
			String title,
			String materialID,
			String channel,
			String taskStatus,
			String taskStart,
			String taskFinish,
			String externalCompliance)
	{
		super();
		this.dateRange = dateRange;
		this.title = title;
		this.materialID = materialID;
		this.channel = channel;
		this.taskStatus = taskStatus;
		this.taskStart = taskStart;
		this.taskFinish = taskFinish;
		this.externalCompliance = externalCompliance;
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

	public String getTaskStart()
	{
		return taskStart;
	}

	public void setTaskStart(String taskStart)
	{
		this.taskStart = taskStart;
	}

	public String getTaskFinish()
	{
		return taskFinish;
	}

	public void setTaskFinish(String taskFinish)
	{
		this.taskFinish = taskFinish;
	}

	public String getExternalCompliance()
	{
		return externalCompliance;
	}

	public void setExternalCompliance(String externalCompliance)
	{
		this.externalCompliance = externalCompliance;
	}
	
	
}
