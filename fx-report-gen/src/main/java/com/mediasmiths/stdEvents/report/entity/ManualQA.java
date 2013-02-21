package com.mediasmiths.stdEvents.report.entity;

public class ManualQA
{
	String dateRange;
	String title;
	String materialID;
	String channel;
	String operator;
	String aggregatorID;
	String taskStatus;
	String previewStatus;
	String hrPreview;
	String hrPreviewRequested;
	String escalated;
	String timeEscalated;
	String titleLength;
	String reordered;
	
	public ManualQA ()
	{
	}
	
	public ManualQA(String title, String materialID)
	{
		super();
		this.title = title;
		this.materialID = materialID;
	}

	public ManualQA(
			String dateRange,
			String title,
			String materialID,
			String channel,
			String operator,
			String aggregatorID,
			String taskStatus,
			String previewStatus,
			String hrPreview,
			String hrPreviewRequested,
			String escalated,
			String timeEscalated,
			String titleLength,
			String reordered)
	{
		super();
		this.dateRange = dateRange;
		this.title = title;
		this.materialID = materialID;
		this.channel = channel;
		this.operator = operator;
		this.aggregatorID = aggregatorID;
		this.taskStatus = taskStatus;
		this.previewStatus = previewStatus;
		this.hrPreview = hrPreview;
		this.hrPreviewRequested = hrPreviewRequested;
		this.escalated = escalated;
		this.timeEscalated = timeEscalated;
		this.titleLength = titleLength;
		this.reordered = reordered;
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

	public String getTaskStatus()
	{
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus)
	{
		this.taskStatus = taskStatus;
	}

	public String getPreviewStatus()
	{
		return previewStatus;
	}

	public void setPreviewStatus(String previewStatus)
	{
		this.previewStatus = previewStatus;
	}

	public String getHrPreview()
	{
		return hrPreview;
	}

	public void setHrPreview(String hrPreview)
	{
		this.hrPreview = hrPreview;
	}

	public String getHrPreviewRequested()
	{
		return hrPreviewRequested;
	}

	public void setHrPreviewRequested(String hrPreviewRequested)
	{
		this.hrPreviewRequested = hrPreviewRequested;
	}

	public String getEscalated()
	{
		return escalated;
	}

	public void setEscalated(String escalated)
	{
		this.escalated = escalated;
	}

	public String getTimeEscalated()
	{
		return timeEscalated;
	}

	public void setTimeEscalated(String timeEscalated)
	{
		this.timeEscalated = timeEscalated;
	}

	public String getTitleLength()
	{
		return titleLength;
	}

	public void setTitleLength(String titleLength)
	{
		this.titleLength = titleLength;
	}

	public String getReordered()
	{
		return reordered;
	}

	public void setReordered(String reordered)
	{
		this.reordered = reordered;
	}
	
	
}