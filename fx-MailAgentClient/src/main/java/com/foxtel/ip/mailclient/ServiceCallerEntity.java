package com.foxtel.ip.mailclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceCallerEntity
{
	@XmlElement
	public String namespace;
	@XmlElement
	public String eventName;
	@XmlElement
	public String payload;
	@XmlElement
	public String comment;

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}

	public String getEventName()
	{
		return eventName;
	}

	public String getPayload()
	{
		return payload;
	}

	public void setPayload(String payload)
	{
		this.payload = payload;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getComment()
	{
		return comment;
	}

}
