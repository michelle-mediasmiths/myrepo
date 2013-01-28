package com.mediasmiths.stdEvents.report.entity;

public class OrderStatus
{
	String status;
	String titleID;
	String orderRef;
	String channel;
	String aggregatorID;

	public String getStatus()
	{
		return status;
	}
	
	public void SetStatus(String status)
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
}
