package com.mediasmiths.stdEvents.reporting.csv;

import org.joda.time.DateTime;

public class DiskUsageReportEntry
{
	private DateTime date;
	private String channel;
	private String hrSize;
	private String tsmSize;
	private String lrSize;
	private String othersSize;
	private String totalSize;


	public void setTotalSize(final String totalSize)
	{
		this.totalSize = totalSize;
	}


	public void setDate(final DateTime date)
	{
		this.date = date.withMillisOfSecond(0).withSecondOfMinute(0);
	}


	public void setChannel(final String channel)
	{
		this.channel = channel;
	}


	public void setHrSize(final String hrSize)
	{
		this.hrSize = hrSize;
	}


	public void setTsmSize(final String tsmSize)
	{
		this.tsmSize = tsmSize;
	}


	public void setLrSize(final String lrSize)
	{
		this.lrSize = lrSize;
	}


	public void setOthersSize(final String othersSize)
	{
		this.othersSize = othersSize;
	}


	public DateTime getDate()
	{
		return date;
	}


	public String getChannel()
	{
		return channel;
	}


	public String getHrSize()
	{
		return hrSize;
	}


	public String getTsmSize()
	{
		return tsmSize;
	}


	public String getLrSize()
	{
		return lrSize;
	}


	public String getOthersSize()
	{
		return othersSize;
	}


	public String getTotalSize()
	{
		return totalSize;
	}
}
