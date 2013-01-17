package com.mediasmiths.stdEvents.events.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;


@Entity
@Table(name="log")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement (name="log")
public class LogEntity
{
	@Id
	@GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	Long logId;
	
	@Column(name="loglevel")
	@XmlElement
	public String level;
	
	@Column(name="message")
	@XmlElement
	@Type(type="text")
	public String message;

	@OneToOne
	@PrimaryKeyJoinColumn
	public EventEntity event;
	
	public String getLevel()
	{
		return level;
	}
	public void setLevel(String level)
	{
		this.level = level;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
