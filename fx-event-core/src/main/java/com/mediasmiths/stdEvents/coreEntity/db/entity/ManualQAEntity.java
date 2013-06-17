package com.mediasmiths.stdEvents.coreEntity.db.entity;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ManualQA")
public class ManualQAEntity
{

	private final static Logger log = Logger.getLogger(ManualQAEntity.class);

	@Id
	private String materialid;
	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	private OrderStatus orderStatus;
	@Basic
	private String operator;
	@Basic
	private String taskStatus;
	@Basic
	private String previewStatus;
	@Basic
	private Boolean hrPreview;
	@Basic
	private String hrPreviewRequestedBy;
	@Basic
	private Integer escalated = 0;
	@Basic
	private Boolean everEscalated = false;
	@Basic
	private Boolean reordered=false;
	@Temporal(TemporalType.TIMESTAMP)
	private Date taskCreated; //date qc task created
	@Temporal(TemporalType.TIMESTAMP)
	private Date taskUpdated; //date qc task created
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeEscalatedSet; //the time that the task was marked as escalated
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeEscalatedCleared; //the time that the task was no longer marked as escalated


	public String getMaterialid()
	{
		return materialid;
	}


	public void setMaterialid(final String materialid)
	{
		this.materialid = materialid;
	}


	public OrderStatus getOrderStatus()
	{
		return orderStatus;
	}


	public void setOrderStatus(final OrderStatus orderStatus)
	{
		this.orderStatus = orderStatus;
	}


	public String getOperator()
	{
		return operator;
	}


	public void setOperator(final String operator)
	{
		this.operator = operator;
	}


	public String getTaskStatus()
	{
		return taskStatus;
	}


	public void setTaskStatus(final String taskStatus)
	{
		this.taskStatus = taskStatus;
	}


	public String getPreviewStatus()
	{
		return previewStatus;
	}


	public void setPreviewStatus(final String previewStatus)
	{
		this.previewStatus = previewStatus;
	}


	public Boolean getHrPreview()
	{
		return hrPreview;
	}


	public void setHrPreview(final Boolean hrPreview)
	{
		this.hrPreview = hrPreview;
	}


	public String getHrPreviewRequestedBy()
	{
		return hrPreviewRequestedBy;
	}


	public void setHrPreviewRequestedBy(final String hrPreviewRequestedBy)
	{
		this.hrPreviewRequestedBy = hrPreviewRequestedBy;
	}


	public Boolean getReordered()
	{
		return reordered;
	}


	public void setReordered(final Boolean reordered)
	{
		this.reordered = reordered;
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


	public Integer getEscalated()
	{
		return escalated;
	}


	public void setEscalated(final Integer escalated)
	{
		this.escalated = escalated;
	}


	public Date getTimeEscalatedSet()
	{
		return timeEscalatedSet;
	}


	public void setTimeEscalatedSet(final Date timeEscalatedSet)
	{
		this.timeEscalatedSet = timeEscalatedSet;
	}


	public Date getTimeEscalatedCleared()
	{
		return timeEscalatedCleared;
	}


	public void setTimeEscalatedCleared(final Date timeEscalatedCleared)
	{
		this.timeEscalatedCleared = timeEscalatedCleared;
	}


	public Boolean getEverEscalated()
	{
		return everEscalated;
	}


	public void setEverEscalated(final Boolean everEscalated)
	{
		this.everEscalated = everEscalated;
	}


	@Transient
	public String getAssetTitle()
	{
		if (orderStatus != null && orderStatus.getTitle() != null)
		{
			return orderStatus.getTitle().getTitle();
		}
		else
		{
			return null;
		}
	}


	@Transient
	public List<String> getChannelsList()
	{

		if (orderStatus != null && orderStatus.getTitle() != null)
		{
			return orderStatus.getTitle().getChannels();
		}
		else
		{
			return Collections.<String>emptyList();
		}
	}


	@Transient
	public String getAggregator()
	{
		if (orderStatus != null)
		{
			return orderStatus.getAggregatorID();
		}
		else
		{
			return null;
		}
	}


	@Transient
	public Interval getTimeEscalatedFor()
	{

		if (getTimeEscalatedSet() == null)
		{
			return null;
		}

		try
		{
			Date timeEscalatedSet1 = getTimeEscalatedSet();
			Date timeEscalatedCleared1 = getTimeEscalatedCleared();

			if (timeEscalatedCleared1 == null)
			{
				timeEscalatedCleared1 = new Date(); //assumed to be still escalated
			}

			DateTime set = new DateTime(timeEscalatedSet1);
			DateTime cleared = new DateTime(timeEscalatedCleared1);

			Interval time = new Interval(set, cleared);
			return time;
		}
		catch (Exception e)
		{
			log.error("error calculating escalated time", e);
			return null;
		}
	}


	@Transient
	public Integer getTitleLength()
	{
		if (orderStatus != null)
		{
			return orderStatus.getTitleLength();
		}
		else
		{
			return null;
		}
	}



}
