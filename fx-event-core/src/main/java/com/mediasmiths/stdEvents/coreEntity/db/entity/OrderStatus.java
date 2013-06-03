package com.mediasmiths.stdEvents.coreEntity.db.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name="OrderStatus")
public class OrderStatus
{
	@Id
	private String materialid;
	@ManyToOne(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
	private Title title;
	@Basic
	private String orderReference;
	@Temporal(TemporalType.DATE)
	private Date created; //date this order was created
	@Temporal(TemporalType.DATE)
	private Date requiredBy; //date content is required by
	@Temporal(TemporalType.DATE)
	private Date completed; //date completed
	@Basic
	private String aggregatorID;
	@Enumerated(EnumType.STRING)
	private TaskType taskType = TaskType.INGEST; //task type, ingest or unmatched (defaults to ingest)

	@Transient
	private transient Boolean complete;
	@Transient
	private transient Boolean overdue;

	public String getMaterialid()
	{
		return materialid;
	}

	public void setMaterialid(String materialid)
	{
		this.materialid = materialid;
	}

	public Title getTitle()
	{
		return title;
	}

	public void setTitle(Title title)
	{
		this.title = title;
	}

	public String getOrderReference()
	{
		return orderReference;
	}

	public void setOrderReference(String orderReference)
	{
		this.orderReference = orderReference;
	}

	public Date getCreated()
	{
		return created;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public Date getRequiredBy()
	{
		return requiredBy;
	}

	public void setRequiredBy(Date requiredBy)
	{
		this.requiredBy = requiredBy;
	}

	public Date getCompleted()
	{
		return completed;
	}

	public void setCompleted(Date completed)
	{
		this.completed = completed;
	}

	public String getAggregatorID()
	{
		return aggregatorID;
	}

	public void setAggregatorID(String aggregatorID)
	{
		this.aggregatorID = aggregatorID;
	}

	public TaskType getTaskType()
	{
		return taskType;
	}

	public void setTaskType(TaskType taskType)
	{
		this.taskType = taskType;
	}

	public Boolean getComplete()
	{
		return complete;
	}

	public void setComplete(Boolean complete)
	{
		this.complete = complete;
	}

	public Boolean getOverdue()
	{
		return overdue;
	}

	public void setOverdue(Boolean overdue)
	{
		this.overdue = overdue;
	}

	public enum TaskType{INGEST,UNMATCHED}
}
