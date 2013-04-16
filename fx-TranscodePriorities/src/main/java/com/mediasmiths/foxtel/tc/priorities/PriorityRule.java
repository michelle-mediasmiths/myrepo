package com.mediasmiths.foxtel.tc.priorities;

public class PriorityRule
{
	private final Long threshold;

	public Long getThreshold()
	{
		return threshold;
	}

	public Integer getPriority()
	{
		return priority;
	}

	private final Integer priority;

	public PriorityRule(Long threshold, Integer priority)
	{
		this.threshold = threshold;
		this.priority = priority;
	}
	
	@Override
	public String toString(){
		return String.format("threshold %d priority %d",getThreshold(),getPriority());
	}

}
