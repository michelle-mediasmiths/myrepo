package com.mediasmiths.foxtel.fs.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MoveRequest
{
	private String from;
	private String to;
	
	@XmlElement(required = true)
	public String getFrom()
	{
		return from;
	}
	public void setFrom(String from)
	{
		this.from = from;
	}
	
	@XmlElement(required = true)
	public String getTo()
	{
		return to;
	}
	public void setTo(String to)
	{
		this.to = to;
	}
	
}
