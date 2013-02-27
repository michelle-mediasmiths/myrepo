package com.mediasmiths.stdEvents.report.entity;

public class OrderStatusRT
{
	String dateRange;
	String status;
	String title;
	String materialID;
	String channel;
	String orderRef;
	String requiredBy;
	String completedInDateRange;
	String overdueInDateRange;
	String aggregatorID;
	String taskType;
	String completionDate;
	
	public OrderStatusRT()
	{
	}
	
	public OrderStatusRT(String t, String mID)
	{
		title = t;
		materialID = mID;
	}

	public OrderStatusRT(
			String dateRange,
			String title,
			String materialID,
			String channel,
			String orderRef,
			String requiredBy,
			String completedInDateRange,
			String overdueInDateRange,
			String aggregatorID,
			String taskType,
			String completionDate)
	{
		super();
		this.dateRange = dateRange;
		this.title = title;
		this.materialID = materialID;
		this.channel = channel;
		this.orderRef = orderRef;
		this.requiredBy = requiredBy;
		this.completedInDateRange = completedInDateRange;
		this.overdueInDateRange = overdueInDateRange;
		this.aggregatorID = aggregatorID;
		this.taskType = taskType;
		this.completionDate = completionDate;
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

	public String getOrderRef()
	{
		return orderRef;
	}

	public void setOrderRef(String orderRef)
	{
		this.orderRef = orderRef;
	}

	public String getRequiredBy()
	{
		return requiredBy;
	}

	public void setRequiredBy(String requiredBy)
	{
		this.requiredBy = requiredBy;
	}

	public String getCompletedInDateRange()
	{
		return completedInDateRange;
	}

	public void setCompletedInDateRange(String completedInDateRange)
	{
		this.completedInDateRange = completedInDateRange;
	}

	public String getOverdueInDateRange()
	{
		return overdueInDateRange;
	}

	public void setOverdueInDateRange(String overdueInDateRange)
	{
		this.overdueInDateRange = overdueInDateRange;
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

	public String getCompletionDate()
	{
		return completionDate;
	}

	public void setCompletionDate(String completionDate)
	{
		this.completionDate = completionDate;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
	
	
	
}