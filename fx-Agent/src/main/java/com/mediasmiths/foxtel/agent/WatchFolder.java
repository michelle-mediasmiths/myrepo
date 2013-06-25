package com.mediasmiths.foxtel.agent;

public class WatchFolder
{

	private String source; //location to watch for files in
	private String delivery; //delivery location (if applicable to current agent)
	private boolean isAO;
	private boolean isRuzz = false;
	private Long stabilityTime = null;
	private String name;


	public boolean isRuzz()
	{
		return isRuzz;
	}


	public void setRuzz(boolean isRuzz)
	{
		this.isRuzz = isRuzz;
	}


	public boolean isAO()
	{
		return isAO;
	}


	public void setAO(boolean isAO)
	{
		this.isAO = isAO;
	}


	public String getSource()
	{
		return source;
	}


	public void setSource(String source)
	{
		this.source = source;
	}


	public String getDelivery()
	{
		return delivery;
	}


	public void setDelivery(String delivery)
	{
		this.delivery = delivery;
	}


	public WatchFolder(String source)
	{
		setSource(source);
	}


	public void setStabilityTime(Long stabilityTime)
	{
		this.stabilityTime = stabilityTime;
	}


	public Long getStabilitytime()
	{
		return this.stabilityTime;
	}

	public String getName()
	{
		return name;
	}


	public void setName(final String name)
	{
		this.name = name;
	}
}
