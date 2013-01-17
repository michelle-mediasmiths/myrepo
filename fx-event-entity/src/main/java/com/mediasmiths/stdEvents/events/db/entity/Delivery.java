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
public class Delivery extends HibernateEventingMessage
{
	@Id
	@GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	Long deliveryId;
	
	@Column(name="titleId")
    @XmlElement
    public String titleID;
	
	@OneToOne
	@PrimaryKeyJoinColumn
	public EventEntity event;

	public String getTitleID()
	{
		return titleID;
	}

	public void setTitleID(String titleID)
	{
		this.titleID = titleID;
	}
}
