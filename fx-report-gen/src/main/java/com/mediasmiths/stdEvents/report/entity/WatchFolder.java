package com.mediasmiths.stdEvents.report.entity;

public class WatchFolder
{
	String dateRange;
	String filename;
	String timeDiscovered;
	String timeProcessed;
	String aggregator;
	
	public WatchFolder()
	{
	}

	public WatchFolder(String filename, String timeDiscovered)
	{
		super();
		this.filename = filename;
		this.timeDiscovered = timeDiscovered;
	}

	public WatchFolder(String dateRange, String filename, String timeDiscovered, String timeProcessed, String aggregator)
	{
		super();
		this.dateRange = dateRange;
		this.filename = filename;
		this.timeDiscovered = timeDiscovered;
		this.timeProcessed = timeProcessed;
		this.aggregator = aggregator;
	}

	public String getDateRange()
	{
		return dateRange;
	}

	public void setDateRange(String dateRange)
	{
		this.dateRange = dateRange;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getTimeDiscovered()
	{
		return timeDiscovered;
	}

	public void setTimeDiscovered(String timeDiscovered)
	{
		this.timeDiscovered = timeDiscovered;
	}

	public String getTimeProcessed()
	{
		return timeProcessed;
	}

	public void setTimeProcessed(String timeProcessed)
	{
		this.timeProcessed = timeProcessed;
	}

	public String getAggregator()
	{
		return aggregator;
	}

	public void setAggregator(String aggregator)
	{
		this.aggregator = aggregator;
	}
	

}
