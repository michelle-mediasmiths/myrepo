package com.mediasmiths.stdEvents.coreEntity.db.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="AutoQC")
public class AutoQC
{
	@Id
	private String materialid;
	@OneToOne(optional=true)
	@PrimaryKeyJoinColumn
	private OrderStatus orderStatus;
	@Basic
	private String assetTitle;
	@Basic
	private String contentType;
	@Basic
	private String operator;
	@Basic
	private String taskStatus;
	@Basic
	private String qcStatus;
	@Temporal(TemporalType.TIMESTAMP)
	private Date taskCreated; //date qc task created
	@Temporal(TemporalType.TIMESTAMP)
	private Date taskFinished; //date qc task finished
	@Temporal(TemporalType.TIMESTAMP)
	private Date warningTime; //date qc task got a WARNING status
	@Basic
	private Boolean override; //true if a user overrode a qc warning
	@Basic
	private String failureParameter;
	
	
	public String getMaterialid()
	{
		return materialid;
	}
	public void setMaterialid(String materialid)
	{
		this.materialid = materialid;
	}
	public OrderStatus getOrderStatus()
	{
		return orderStatus;
	}
	public void setOrderStatus(OrderStatus orderStatus)
	{
		this.orderStatus = orderStatus;
	}
	public String getAssetTitle()
	{
		return assetTitle;
	}
	public void setAssetTitle(String assetTitle)
	{
		this.assetTitle = assetTitle;
	}
	public String getContentType()
	{
		return contentType;
	}
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
	public String getOperator()
	{
		return operator;
	}
	public void setOperator(String operator)
	{
		this.operator = operator;
	}
	public String getTaskStatus()
	{
		return taskStatus;
	}
	public void setTaskStatus(String taskStatus)
	{
		this.taskStatus = taskStatus;
	}
	public Date getCreatedTime()
	{
		return taskCreated;
	}
	public void setTaskCreated(Date taskCreated)
	{
		this.taskCreated = taskCreated;
	}
	public Date getTaskFinishedTime()
	{
		return taskFinished;
	}
	public void setTaskFinished(Date taskFinished)
	{
		this.taskFinished = taskFinished;
	}
	public Date getWarningTime()
	{
		return warningTime;
	}
	public void setWarningTime(Date warningTime)
	{
		this.warningTime = warningTime;
	}
	public Boolean getOverride()
	{
		return override;
	}
	public void setOverride(Boolean override)
	{
		this.override = override;
	}
	public String getFailureParameter()
	{
		return failureParameter;
	}
	public void setFailureParameter(String failureParameter)
	{
		this.failureParameter = failureParameter;
	}
	public String getQcStatus()
	{
		return qcStatus;
	}
	public void setQcStatus(String qcStatus)
	{
		this.qcStatus = qcStatus;
	}
	
}
