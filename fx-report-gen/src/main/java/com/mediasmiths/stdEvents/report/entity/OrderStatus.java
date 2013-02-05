package com.mediasmiths.stdEvents.report.entity;

public class OrderStatus
{
	String dateRange;
	String status;
	String titleID;
	String orderRef;
	String channel;
	String aggregatorID;
	String taskType;
	String ingestDate;
	String requiredBy;
	
	public OrderStatus()
	{
	}
	
	public OrderStatus(String s, String tID)
	{
		status = s;
		titleID = tID;
	}
	
	public OrderStatus(
			String dateRange,
			String status,
			String titleID,
			String orderRef,
			String channel,
			String aggregatorID,
			String taskType,
			String ingestDate,
			String requiredBy)
	{
		super();
		this.dateRange = dateRange;
		this.status = status;
		this.titleID = titleID;
		this.orderRef = orderRef;
		this.channel = channel;
		this.aggregatorID = aggregatorID;
		this.taskType = taskType;
		this.ingestDate = ingestDate;
		this.requiredBy = requiredBy;
	}

	public String getDateRange()
	{
		return dateRange;
	}

	public void setDateRange(String dateRange)
	{
		this.dateRange = dateRange;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getTitleID()
	{
		return titleID;
	}

	public void setTitleID(String titleID)
	{
		this.titleID = titleID;
	}

	public String getOrderRef()
	{
		return orderRef;
	}

	public void setOrderRef(String orderRef)
	{
		this.orderRef = orderRef;
	}

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public String getAggregatorID()
	{
		return aggregatorID;
	}

	public void setAggregatorID(String aggregatorID)
	{
		this.aggregatorID = aggregatorID;
	}

	public String getTaskType()
	{
		return taskType;
	}

	public void setTaskType(String taskType)
	{
		this.taskType = taskType;
	}

	public String getIngestDate()
	{
		return ingestDate;
	}

	public void setIngestDate(String ingestDate)
	{
		this.ingestDate = ingestDate;
	}

	public String getRequiredBy()
	{
		return requiredBy;
	}

	public void setRequiredBy(String requiredBy)
	{
		this.requiredBy = requiredBy;
	}

	
}
