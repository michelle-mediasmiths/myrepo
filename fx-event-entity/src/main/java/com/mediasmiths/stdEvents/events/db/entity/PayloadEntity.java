package com.mediasmiths.stdEvents.events.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;


@Entity
@Table(name="payload")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement (name="payload")
public class PayloadEntity extends HibernateEventingMessage
{	
	@Id
	@GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	Long payloadId;
	
	@Column(name="information")
	@XmlElement
	@Lob
	String information;
	
	@Column(name="content")
	@XmlElement
	@Type(type="text")
	String content;
	
	@OneToOne
	@PrimaryKeyJoinColumn
	public EventEntity event;

	
	public String getInformation () {
		return information;
	}
	public void setInformation (String information) {
		this.information = information;
	}
	
	public String getContent () {
		return content;
	}
	public void setContent (String content) {
		this.content = content;
	}
}
