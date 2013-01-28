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

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.HibernateEventingMessage;

@Entity
@Table(name="delivery")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement (name="Delivery")
public class DeliveryDetails extends HibernateEventingMessage
{
	@Id
	@GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	Long deliveryId;
	
	@Column(name="master_id")
    @XmlElement
    public String masterId;
	
	@Column(name="title")
	@XmlElement
	public String title;

	@Column(name="file_location")
	@XmlElement
	public String fileLocation;

	@Column(name="delivered")
	@XmlElement
	public boolean delivered;
	
	@OneToOne
	@PrimaryKeyJoinColumn
	public EventEntity event;

	public String getMasterId()
	{
		return masterId;
	}

	public void setMasterId(String masterId)
	{
		this.masterId = masterId;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getFileLocation()
	{
		return fileLocation;
	}

	public void setFileLocation(String fileLocation)
	{
		this.fileLocation = fileLocation;
	}

	public boolean isDelivered()
	{
		return delivered;
	}

	public void setDelivered(boolean delivered)
	{
		this.delivered = delivered;
	}
}
