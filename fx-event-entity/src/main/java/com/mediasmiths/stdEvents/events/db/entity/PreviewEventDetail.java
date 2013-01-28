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
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;


/**
 * A Preview has been performed and this captures the
 * 					outcome
 * 				
 * 
 * <p>Java class for PreviewEventDetail complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PreviewEventDetail">
 *   &lt;complexContent>
 *     &lt;extension base="{http://com.mediasmiths.foxtel.ip.common.events/001/000}IPEvent">
 *       &lt;sequence>
 *         &lt;element name="masterId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="op" type="{http://com.mediasmiths.foxtel.ip.common.events/001/000}PreviewStatus"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

@Entity
@Table(name="preview")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PreviewEventDetail
{
	@Id
	@GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	Long previewId;
	
	@Column(name="master_id")
	@XmlElement
	public String masterId;
	
	@Column(name="title")
	@XmlElement
	public String title;
	
	@Column(name="preview_status")
	@XmlElement
	public String previewStatus;
	
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

	public String getPreviewStatus()
	{
		return previewStatus;
	}

	public void setPreviewStatus(String previewStatus)
	{
		this.previewStatus = previewStatus;
	}
}
