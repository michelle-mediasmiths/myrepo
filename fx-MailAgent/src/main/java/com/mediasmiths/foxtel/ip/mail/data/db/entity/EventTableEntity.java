package com.mediasmiths.foxtel.ip.mail.data.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

@Entity
@Table(name="event")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EventTableEntity 
{

	/**
	 * Creates the columns for the 'event' table
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	public Long id;

	@Column(name = "TIME", nullable = false)
	@XmlElement
	public long time;
	
	@Column (name="NAMESPACE")
	@XmlElement
	public String namespace;

	@Column(name = "EVENT_NAME")
	@XmlElement
	public String eventName;

	@Column(name="PAYLOAD", length=4000)
	@XmlElement
	public String payload;
	
	@Column(name="CONTENT")
	@XmlElement
	public String content;
	
	@Column(name="EVENT_TIME")
	@XmlElement
	public Long eventTime;
	
	/**
	 * Getters and setters
	 */

	public long getTime()
	{
		return time;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public String getEventName()
	{
		return eventName;
	}

	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}

	public String getPayload()
	{
		return payload;
	}

	public void setPayload(String payload)
	{
		this.payload = payload;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public Long getEventTime()
	{
		return eventTime;
	}

	public void setEventTime(Long eventTime)
	{
		this.eventTime = eventTime;
	}

}
