package com.mediasmiths.stdEvents.events.db.entity.placeholder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.HibernateEventingMessage;

@Entity
@Table(name="manualPurge")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="ManualPurge")
public class ManualPurge extends HibernateEventingMessage
{
	@Id
	@GeneratedValue(generator="gen")
	@GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	Long manualPurgeId;

	@Column(name="title_id")
	@XmlElement
	public String titleID;

	@Column(name="master_id")
	@XmlElement
	public String masterID;

	@Column(name="purge_status")
	@XmlElement
	public String purgeStatus;

	@Column(name="protected_status")
	@XmlElement
	public String protectedStatus;

	@Column(name="extended_status")
	@XmlElement
	public String extendedStatus;

	@Column(name="expire_date")
	@XmlElement
	public String expireDate;

	@Column(name="deleted_in")
	@XmlElement
	public String deletedIn;

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

	public String getMasterID()
	{
		return masterID;
	}

	public void setMasterID(String masterID)
	{
		this.masterID = masterID;
	}

	public String getPurgeStatus()
	{
		return purgeStatus;
	}

	public void setPurgeStatus(String purgeStatus)
	{
		this.purgeStatus = purgeStatus;
	}

	public String getProtectedStatus()
	{
		return protectedStatus;
	}

	public void setProtectedStatus(String protectedStatus)
	{
		this.protectedStatus = protectedStatus;
	}

	public String getExtendedStatus()
	{
		return extendedStatus;
	}

	public void setExtendedStatus(String extendedStatus)
	{
		this.extendedStatus = extendedStatus;
	}

	public String getExpireDate()
	{
		return expireDate;
	}

	public void setExpireDate(String expireDate)
	{
		this.expireDate = expireDate;
	}

	public String getDeletedIn()
	{
		return deletedIn;
	}

	public void setDeletedIn(String deletedIn)
	{
		this.deletedIn = deletedIn;
	}

}
