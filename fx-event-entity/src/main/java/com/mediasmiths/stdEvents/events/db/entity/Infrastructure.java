package com.mediasmiths.stdEvents.events.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;

import org.hibernate.annotations.Parameter;

import org.hibernate.annotations.GenericGenerator;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

/**
 * The Details of Storage use for a particular mount point
 * 
 * <p>Java class for StorageUsageDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StorageUsageDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fileSystemReference" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="megaBytesUsed" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="megaBytesFree" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

@Entity
@Table(name="infrastructure")
@XmlAccessorType(XmlAccessType.FIELD)
public class Infrastructure
{
	@Id
	@GeneratedValue(generator="gen")
	@GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	Long infrastructureId;

	@Column(name="file_system_reference")
	@XmlElement
	public String fileSystemReference;

	@Column(name="mega_bytes_used")
	@XmlElement
	public String megaBytesUsed;

	@Column(name="mega_bytes_free")
	@XmlElement
	public String megaBytesFree;

	@OneToOne
	@PrimaryKeyJoinColumn
	public EventEntity event;

	public Long getInfrastructureId()
	{
		return infrastructureId;
	}

	public void setInfrastructureId(Long infrastructureId)
	{
		this.infrastructureId = infrastructureId;
	}

	public String getFileSystemReference()
	{
		return fileSystemReference;
	}

	public void setFileSystemReference(String fileSystemReference)
	{
		this.fileSystemReference = fileSystemReference;
	}

	public String getMegaBytesUsed()
	{
		return megaBytesUsed;
	}

	public void setMegaBytesUsed(String megaBytesUsed)
	{
		this.megaBytesUsed = megaBytesUsed;
	}

	public String getMegaBytesFree()
	{
		return megaBytesFree;
	}

	public void setMegaBytesFree(String megaBytesFree)
	{
		this.megaBytesFree = megaBytesFree;
	}


}
