package com.mediasmiths.foxtel.ip.mail.data.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

@Entity
@Table(name = "eventing")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EventingTableEntity
{

	/**
	 * Creates the columns for the 'event' table
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Column(name = "EVENTID")
	private Long eventId;
	
	/**
	 * Getters and setters
	 */
	
	public Long getEventId()
	{
		return eventId;
	}

	public void setEventId(Long eventId)
	{
		this.eventId = eventId;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

}
