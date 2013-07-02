package com.mediasmiths.stdEvents.coreEntity.db.entity;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;

@Entity
@Table(name="Transcode")
public class TranscodeJob
{

	private static final Logger log = Logger.getLogger(TranscodeJob.class);

	@Id
	private String jobID;
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;
	@Temporal(TemporalType.TIMESTAMP)
	private Date started;
	@Basic
	private String sourceFormat;
	@Basic
	private String destinationFormat;
	@Basic
	private Integer priority;
	@Basic
	private String status;


	public String getJobID()
	{
		return jobID;
	}


	public void setJobID(final String jobID)
	{
		this.jobID = jobID;
	}


	public Date getCreated()
	{
		return created;
	}


	public void setCreated(final Date created)
	{
		this.created = created;
	}


	public Date getUpdated()
	{
		return updated;
	}


	public void setUpdated(final Date updated)
	{
		this.updated = updated;
	}


	public Date getStarted()
	{
		return started;
	}


	public void setStarted(final Date started)
	{
		this.started = started;
	}


	public String getSourceFormat()
	{
		return sourceFormat;
	}


	public void setSourceFormat(final String sourceFormat)
	{
		this.sourceFormat = sourceFormat;
	}


	public String getDestinationFormat()
	{
		return destinationFormat;
	}


	public void setDestinationFormat(final String destinationFormat)
	{
		this.destinationFormat = destinationFormat;
	}


	public Integer getPriority()
	{
		return priority;
	}


	public void setPriority(final Integer priority)
	{
		this.priority = priority;
	}


	public String getStatus()
	{
		return status;
	}


	public void setStatus(final String status)
	{
		this.status = status;
	}


	@Transient
	public boolean isFailed()
	{

		if ("Fatal".equals(getStatus()) || "FATAL".equals(getStatus()))
		{
			return true;
		}
		return false;
	}


	@Transient
	public boolean isSuccess()
	{

		if ("Completed".equals(getStatus()) || "COMPLETED".equals(getStatus()))
		{
			return true;
		}
		return false;
	}


	@Transient
	public Long getQueuedTime()
	{

		DateTime created = new DateTime(getCreated());
		DateTime started = new DateTime(getStarted());

		if (started == null)
		{
			log.debug("no start time for this job");
			return null;
		}

		if (created.isAfter(started))
		{
			log.warn("create time after start time for job " + jobID);
			return null;
		}
		else
		{
			return new Duration(created, started).getMillis();
		}
	}


	@Transient
	public Long getTranscodeTime()
	{

		DateTime created = new DateTime(getCreated());
		DateTime updated = new DateTime(getUpdated());

		if (!isSuccess())
		{
			log.debug("returning null transcode time for unsuccessful job");
			return null;
		}

		if (created.isAfter(updated))
		{
			log.warn("create time after update time for job " + jobID);
			return null;
		}
		else
		{
			return new Duration(created, updated).getMillis();
		}
	}
}
