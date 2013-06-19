package com.mediasmiths.stdEvents.coreEntity.db.entity;

import org.apache.log4j.Logger;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "Purge")
public class Purge
{

	private final static Logger log = Logger.getLogger(Purge.class);

	@Id
	private String houseID;
	@Basic
	private String assetType;
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Title title;
	@Basic
	private Boolean everProtected = false;
	@Basic
	private Boolean isProtected = false;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateEntityCreated;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateProtected;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateUnProtected;
	@Basic
	private Boolean isExtended = false;
	@Basic
	private Boolean hasPurgeCandidateTask = false;
	@Temporal(TemporalType.TIMESTAMP)
	private Date datePurgeCandidateTaskCreated;

	@Temporal(TemporalType.TIMESTAMP)
	private Date datePurgeCandidateTaskUpdated;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateExtended;
	@Basic
	private Boolean isPurged = false;
	@Temporal(TemporalType.TIMESTAMP)
	private Date datePurged;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateExpires;


	public String getHouseID()
	{
		return houseID;
	}


	public void setHouseID(final String houseID)
	{
		this.houseID = houseID;
	}


	public String getAssetType()
	{
		return assetType;
	}


	public void setAssetType(final String assetType)
	{
		this.assetType = assetType;
	}


	public Title getTitle()
	{
		return title;
	}


	public void setTitle(final Title title)
	{
		this.title = title;
	}


	public Boolean getProtected()
	{
		return isProtected;
	}


	public void setProtected(final Boolean aProtected)
	{
		isProtected = aProtected;
	}


	public Date getDateProtected()
	{
		return dateProtected;
	}


	public void setDateProtected(final Date dateProtected)
	{
		this.dateProtected = dateProtected;
	}


	public Boolean getExtended()
	{
		return isExtended;
	}


	public void setExtended(final Boolean extended)
	{
		isExtended = extended;
	}


	public Date getDateExtended()
	{
		return dateExtended;
	}


	public void setDateExtended(final Date dateExtended)
	{
		this.dateExtended = dateExtended;
	}


	public Boolean getPurged()
	{
		return isPurged;
	}


	public void setPurged(final Boolean purged)
	{
		isPurged = purged;
	}


	public Date getDatePurged()
	{
		return datePurged;
	}


	public void setDatePurged(final Date datePurged)
	{
		this.datePurged = datePurged;
	}


	public Date getDateExpires()
	{
		return dateExpires;
	}


	public void setDateExpires(final Date dateExpires)
	{
		this.dateExpires = dateExpires;
	}


	public Date getDateEntityCreated()
	{
		return dateEntityCreated;
	}


	public void setDateEntityCreated(final Date dateEntityCreated)
	{
		this.dateEntityCreated = dateEntityCreated;
	}


	public Date getDateUnProtected()
	{
		return dateUnProtected;
	}


	public void setDateUnProtected(final Date dateUnProtected)
	{
		this.dateUnProtected = dateUnProtected;
	}


	public Boolean getEverProtected()
	{
		return everProtected;
	}


	public void setEverProtected(final Boolean everProtected)
	{
		this.everProtected = everProtected;
	}


	public Boolean getHasPurgeCandidateTask()
	{
		return hasPurgeCandidateTask;
	}


	public void setHasPurgeCandidateTask(final Boolean hasPurgeCandidateTask)
	{
		this.hasPurgeCandidateTask = hasPurgeCandidateTask;
	}


	public Date getDatePurgeCandidateTaskUpdated()
	{
		return datePurgeCandidateTaskUpdated;
	}


	public void setDatePurgeCandidateTaskUpdated(final Date datePurgeCandidateTaskUpdated)
	{
		this.datePurgeCandidateTaskUpdated = datePurgeCandidateTaskUpdated;
	}


	public Date getDatePurgeCandidateTaskCreated()
	{
		return datePurgeCandidateTaskCreated;
	}


	public void setDatePurgeCandidateTaskCreated(final Date datePurgeCandidateTaskCreated)
	{
		this.datePurgeCandidateTaskCreated = datePurgeCandidateTaskCreated;
	}
}
