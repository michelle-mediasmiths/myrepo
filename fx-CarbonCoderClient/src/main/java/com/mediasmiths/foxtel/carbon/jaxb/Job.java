package com.mediasmiths.foxtel.carbon.jaxb;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Job")
public class Job
{
	
	private String name;
	private UUID guid;
	
	public String getName()
	{
		return name;
	}
	
	@XmlAttribute(name = "Name")
	public void setName(String name)
	{
		this.name = name;
	}
	public UUID getGuid()
	{
		return guid;
	}
	@XmlAttribute(name = "GUID")
	public void setGuid(UUID guid)
	{
		this.guid = guid;
	}
	
	@Override
	public String toString(){
		return String.format("Name {%s} GUID {%s}", getName(), getGuid().toString());
	}


}