package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetPriorityResponse
{
	@XmlElement(required=true)
	private Integer priority;
	
	@XmlElement(required=true)
	private boolean changed;

	public Integer getPriority()
	{
		return priority;
	}

	public void setPriority(Integer priority)
	{
		this.priority = priority;
	}

	public boolean isChanged()
	{
		return changed;
	}

	public void setChanged(boolean changed)
	{
		this.changed = changed;
	}
}
