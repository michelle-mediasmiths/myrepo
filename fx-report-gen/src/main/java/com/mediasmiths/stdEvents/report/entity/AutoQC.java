package com.mediasmiths.stdEvents.report.entity;

public class AutoQC
{
	String dateRange;
	String title;
	String materialID;
	String channels;
	String contentType;
	String operator;
	String taskStatus;
	String qcStatus;
	String taskStart;
	String manualOverride;
	String failureParameter;
	String titleLength;
	
	public AutoQC() 
	{
	}
	
	public AutoQC(
			String dateRange,
			String title,
			String materialID,
			String channels,
			String content,
			String operator,
			String taskStatus,
			String qcStatus,
			String taskStart,
			String manualOverride,
			String failureParameter,
			String titleLength)
	{
		super();
		this.dateRange = dateRange;
		this.title = title;
		this.materialID = materialID;
		this.channels = channels;
		this.contentType = content;
		this.operator = operator;
		this.taskStatus = taskStatus;
		this.qcStatus = qcStatus;
		this.taskStart = taskStart;
		this.manualOverride = manualOverride;
		this.failureParameter = failureParameter;
		this.titleLength = titleLength;
	}

	public AutoQC(String title, String materialID)
	{
		super();
		this.title = title;
		this.materialID = materialID;
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

	public String getChannels()
	{
		return channels;
	}

	public void setChannels(String channels)
	{
		this.channels = channels;
	}

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	public String getOperator()
	{
		return operator;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	public String getTaskStatus()
	{
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus)
	{
		this.taskStatus = taskStatus;
	}

	public String getQcStatus()
	{
		return qcStatus;
	}

	public void setQcStatus(String qcStatus)
	{
		this.qcStatus = qcStatus;
	}

	public String getTaskStart()
	{
		return taskStart;
	}

	public void setTaskStart(String taskStart)
	{
		this.taskStart = taskStart;
	}

	public String getManualOverride()
	{
		return manualOverride;
	}

	public void setManualOverride(String manualOverride)
	{
		this.manualOverride = manualOverride;
	}

	public String getFailureParameter()
	{
		return failureParameter;
	}

	public void setFailureParameter(String failureParameter)
	{
		this.failureParameter = failureParameter;
	}

	public String getTitleLength()
	{
		return titleLength;
	}

	public void setTitleLength(String titleLength)
	{
		this.titleLength = titleLength;
	}
	
	
	
}
