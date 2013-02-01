package com.mediasmiths.stdEvents.report.entity;

public class ManualQA
{
	String dateRange;
	String title;
	String assetID;
	String operator;
	String aggregatorID;
	String qaStatus;
	String duration;
	
	public ManualQA ()
	{
	}
	
	public ManualQA(String title, String assetID)
	{
		super();
		this.title = title;
		this.assetID = assetID;
	}

	public ManualQA(String dateRange, String title, String assetID, String operator, String aggregatorID, String qaStatus, String duration)
	{
		super();
		this.dateRange = dateRange;
		this.title = title;
		this.assetID = assetID;
		this.operator = operator;
		this.aggregatorID = aggregatorID;
		this.qaStatus = qaStatus;
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

	public String getOperator()
	{
		return operator;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	public String getAggregatorID()
	{
		return aggregatorID;
	}

	public void setAggregatorID(String aggregatorID)
	{
		this.aggregatorID = aggregatorID;
	}

	public String getQaStatus()
	{
		return qaStatus;
	}

	public void setQaStatus(String qaStatus)
	{
		this.qaStatus = qaStatus;
	}

	public String getDuration()
	{
		return duration;
	}

	public void setDuration(String duration)
	{
		this.duration = duration;
	}

	public String getAssetID()
	{
		return assetID;
	}

	public void setAssetID(String assetID)
	{
		this.assetID = assetID;
	}	
}
