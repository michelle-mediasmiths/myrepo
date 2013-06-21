package com.mediasmiths.stdEvents.events.db.entity.nagios;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.HibernateEventingMessage;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

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

@Entity
@Table(name = "NagiosReport")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "NagiosReport")
public class NagiosReportEntity extends HibernateEventingMessage
{
	@Id
	@GeneratedValue(generator = "gen")
	@GenericGenerator(name = "gen", strategy = "foreign", parameters = @Parameter(name = "property", value = "event"))
	Long nagiosReportId;

	@XmlElement
	@Column(name = "notificationType")
	public String notificationType;

	@XmlElement
	@Column(name = "ipAddress")
	public String ipAddress;

	@XmlElement
	@Column(name = "state")
	public String state;

	@XmlElement
	@Column(name = "info")
	public String info;

	@XmlElement
	@Column(name = "currentLoad")
	public String currentLoad;

	@XmlElement
	@Column(name = "programs")
	public String programs;

	@XmlElement
	@Column(name = "swap")
	public String swap;

	@XmlElement
	@Column(name = "users")
	public String users;

	@XmlElement
	@Column(name = "diskUsage")
	public String diskUsage;
	
	@OneToOne
	@PrimaryKeyJoinColumn
	public EventEntity event;

	/**
	 * Getters and setters
	 */

	public String getnotificationType()
	{
		return notificationType;
	}

	public void setnotificationType(String notificationType)
	{
		this.notificationType = notificationType;
	}

	public String getipAddress()
	{
		return ipAddress;
	}

	public void setipAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	public String getstate()
	{
		return state;
	}

	public void setstate(String state)
	{
		this.state = state;
	}

	
	public String getinfo()
	{
		return info;
	}

	public void setinfo(String info)
	{
		this.info = info;
	}

	public String getcurrentLoad()
	{
		return currentLoad;
	}

	public void setcurrentLoad(String currentLoad)
	{
		this.currentLoad = currentLoad;
	}

	public String getprograms()
	{
		return programs;
	}

	public void setprograms(String programs)
	{
		this.programs = programs;
	}

	public String getswap()
	{
		return swap;
	}

	public void setswap(String swap)
	{
		this.swap = swap;
	}

	public String getusers()
	{
		return users;
	}

	public void setusers(String users)
	{
		this.users = users;
	}

	public String getdiskUsage()
	{
		return diskUsage;
	}

	public void setdiskUsage(String diskUsage)
	{
		this.diskUsage = diskUsage;
	}

}
