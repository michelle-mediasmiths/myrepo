package com.mediasmiths.stdEvents.report.entity;

public class AcquisitionRT
{
	String dateRange;
	String title;
	String materialID;
	String channels;
	String aggregatorID;
	String tape;
	String file;
	String format;
	String fileSize;
	String duration;
	
	public AcquisitionRT()
	{
	}
	
	public AcquisitionRT(String title, String materialID)
	{
		super();
		this.title = title;
		this.materialID = materialID;
	}

	public AcquisitionRT(String materialID, String channels, String aggregatorID, String tape, String file,
			String format,String fileSize,String duration, String dateRange, String title)
	{
		super();
		this.dateRange = dateRange;
		this.title = title;
		this.materialID = materialID;
		this.channels = channels;
		this.aggregatorID = aggregatorID;
		this.tape = tape;
		this.file = file;
		this.format = format;
		this.fileSize = fileSize;
		this.duration = duration;
	}


	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDateRange()
	{
		return dateRange;
	}
	public void setDateRange(String dateRange)
	{
		this.dateRange = dateRange;
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
	public String getAggregatorID()
	{
		return aggregatorID;
	}
	public void setAggregatorID(String aggregatorID)
	{
		this.aggregatorID = aggregatorID;
	}
	public String getTape()
	{
		return tape;
	}
	public void setTape(String tape)
	{
		this.tape = tape;
	}
	public String getFile()
	{
		return file;
	}
	public void setFile(String file)
	{
		this.file = file;
	}
	public String getFormat()
	{
		return format;
	}
	public void setFormat(String format)
	{
		this.format = format;
	}
	public String getFileSize()
	{
		return fileSize;
	}
	public void setFileSize(String fileSize)
	{
		this.fileSize = fileSize;
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