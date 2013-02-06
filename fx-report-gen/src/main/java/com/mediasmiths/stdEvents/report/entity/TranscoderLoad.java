package com.mediasmiths.stdEvents.report.entity;

public class TranscoderLoad
{
	String dateRange;
	String mediaID;
	String transcodeFinish;
	String duration;
	String sourceFormat;
	String result;
	
	public TranscoderLoad ()
	{
	}

	public TranscoderLoad(
			String dateRange,
			String mediaID,
			String transcodeFinish,
			String duration,
			String sourceFormat,
			String result)
	{
		super();
		this.dateRange = dateRange;
		this.mediaID = mediaID;
		this.transcodeFinish = transcodeFinish;
		this.duration = duration;
		this.sourceFormat = sourceFormat;
		this.result = result;
	}

	public String getDateRange()
	{
		return dateRange;
	}

	public void setDateRange(String dateRange)
	{
		this.dateRange = dateRange;
	}

	public String getMediaID()
	{
		return mediaID;
	}

	public void setMediaID(String mediaID)
	{
		this.mediaID = mediaID;
	}

	public String getTranscodeFinish()
	{
		return transcodeFinish;
	}

	public void setTranscodeFinish(String transcodeFinish)
	{
		this.transcodeFinish = transcodeFinish;
	}

	public String getDuration()
	{
		return duration;
	}

	public void setDuration(String duration)
	{
		this.duration = duration;
	}

	public String getSourceFormat()
	{
		return sourceFormat;
	}

	public void setSourceFormat(String sourceFormat)
	{
		this.sourceFormat = sourceFormat;
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	public TranscoderLoad(String mediaID, String transcodeFinish)
	{
		super();
		this.mediaID = mediaID;
		this.transcodeFinish = transcodeFinish;
	}
	
	
}
