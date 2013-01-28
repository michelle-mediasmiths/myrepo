package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;

public abstract class AutoQCResultNotification extends AutoQCRequest
{
	private String jobName;

	@XmlElement
	public String getJobName()
	{
		return jobName;
	}

	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}
	
	
}
