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
@Table(name = "ComplianceLogging")
public class ComplianceLogging
{
	private final static Logger log = Logger.getLogger(ComplianceLogging.class);

	@Id
	private Long taskID;
	@Basic
	private String materialID;
	@Basic
	private String sourceMaterialID;
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Title title;
	@Basic
	private Boolean complete = false;
	@Basic
	private String taskStatus;
	@Temporal(TemporalType.TIMESTAMP)
	private Date taskCreated; //date extended compliance task created
	@Temporal(TemporalType.TIMESTAMP)
	private Date taskUpdated; //date extended compliance task updated
	@Temporal(TemporalType.DATE)
	private Date dateCompleted; //date completed


	@Basic
	private Boolean externalCompliance = false;


	public Long getTaskID()
	{
		return taskID;
	}


	public void setTaskID(final Long taskID)
	{
		this.taskID = taskID;
	}


	public String getMaterialID()
	{
		return materialID;
	}


	public void setMaterialID(final String materialID)
	{
		this.materialID = materialID;
	}


	public String getSourceMaterialID()
	{
		return sourceMaterialID;
	}


	public void setSourceMaterialID(final String sourceMaterialID)
	{
		this.sourceMaterialID = sourceMaterialID;
	}


	public Title getTitle()
	{
		return title;
	}


	public void setTitle(final Title title)
	{
		this.title = title;
	}


	public Boolean getComplete()
	{
		return complete;
	}


	public void setComplete(final Boolean complete)
	{
		this.complete = complete;
	}


	public String getTaskStatus()
	{
		return taskStatus;
	}


	public void setTaskStatus(final String taskStatus)
	{
		this.taskStatus = taskStatus;
	}


	public Date getTaskCreated()
	{
		return taskCreated;
	}


	public void setTaskCreated(final Date taskCreated)
	{
		this.taskCreated = taskCreated;
	}


	public Date getTaskUpdated()
	{
		return taskUpdated;
	}


	public void setTaskUpdated(final Date taskUpdated)
	{
		this.taskUpdated = taskUpdated;
	}


	public Date getDateCompleted()
	{
		return dateCompleted;
	}


	public void setDateCompleted(final Date dateCompleted)
	{
		this.dateCompleted = dateCompleted;
	}


	public Boolean getExternalCompliance()
	{
		return externalCompliance;
	}


	public void setExternalCompliance(final Boolean externalCompliance)
	{
		this.externalCompliance = externalCompliance;
	}
}
