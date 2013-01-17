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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.HibernateEventingMessage;


@Entity
@Table(name="request")
@XmlAccessorType(XmlAccessType.FIELD)
public class Request extends HibernateEventingMessage
{
	@Id
	@GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	public Long requestId;
	
	@Column(name = "TIME", nullable = false)
	@XmlElement
	public long time;
	
	@Column (name="NAMESPACE")
	@XmlElement
	public String namespace;
	
	@Column(name = "REQUEST")
	@XmlElement
	public String request;
	
	@Column(name="REQUEST_TYPE")
	@XmlElement
	public String requestType;
	
	@OneToOne
	@PrimaryKeyJoinColumn
	public EventEntity event;

	public Long getRequestId()
	{
		return requestId;
	}

	public void setRequestId(Long requestId)
	{
		this.requestId = requestId;
	}

	public long getTime()
	{
		return time;
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

	public String getRequest()
	{
		return request;
	}

	public void setRequest(String request)
	{
		this.request = request;
	}

	public String getRequestType()
	{
		return requestType;
	}

	public void setRequestType(String requestType)
	{
		this.requestType = requestType;
	}
}
