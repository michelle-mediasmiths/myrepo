package com.mediasmiths.stdEvents.coreEntity.db.entity;

import org.apache.log4j.Logger;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "ExtendedPublishing")
public class ExtendedPublishing
{

	private final static Logger log = Logger.getLogger(ExtendedPublishing.class);

	@Id
	private Long taskID;
	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	private OrderStatus orderStatus;
	@Basic
	private String materialID;
	@Basic
	private String taskStatus;
	@Basic
	private String requestedBy;
	@Basic
	private String exportType;
	@Temporal(TemporalType.TIMESTAMP)
	private Date taskCreated; //date extended publishing task created
	@Temporal(TemporalType.TIMESTAMP)
	private Date taskUpdated; //date extended publishing task updated


	public Long getTaskID()
	{
		return taskID;
	}


	public void setTaskID(final Long taskID)
	{
		this.taskID = taskID;
	}


	public OrderStatus getOrderStatus()
	{
		return orderStatus;
	}


	public void setOrderStatus(final OrderStatus orderStatus)
	{
		this.orderStatus = orderStatus;
	}


	public String getMaterialID()
	{
		return materialID;
	}


	public void setMaterialID(final String materialID)
	{
		this.materialID = materialID;
	}


	public String getTaskStatus()
	{
		return taskStatus;
	}


	public void setTaskStatus(final String taskStatus)
	{
		this.taskStatus = taskStatus;
	}


	public String getExportType()
	{
		return exportType;
	}


	public void setExportType(final String exportType)
	{
		this.exportType = exportType;
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


	public String getRequestedBy()
	{
		return requestedBy;
	}


	public void setRequestedBy(final String requestedBy)
	{
		this.requestedBy = requestedBy;
	}
}
