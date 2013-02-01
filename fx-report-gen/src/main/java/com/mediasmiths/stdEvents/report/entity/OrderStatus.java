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
	
	public OrderStatus()
	{
	}
	
	public OrderStatus(String s, String tID)
	{
		status = s;
		titleID = tID;
	}
	
	public OrderStatus(String dr, String s, String tID, String o, String c, String aID, String tt)
	{
		dateRange = dr;
		status = s;
		titleID = tID;
		orderRef = o;
		channel = c;
		aggregatorID = aID;
		taskType = tt;
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

	
}
