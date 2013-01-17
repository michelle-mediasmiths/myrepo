package com.mediasmiths.stdEvents.events.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "mappertable")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappertable")
public class MapperTableEntity
{

	@Id
	@GeneratedValue(generator = "gen")
	@GenericGenerator(name = "gen", strategy = "foreign", parameters = @Parameter(name = "property", value = "event"))
	Long mapperID;

	@Column(name = "namespace")
	@XmlElement(name = "namespace", required = true)
	public String namespace;

	@Column(name = "eventname")
	@XmlElement(name = "eventname")
	public String eventname;

	@Column(name = "endpoint")
	@XmlElement(name = "endpoint", required = true)
	public String endpoint;

	@Column(name = "config", length = 4000)
	@XmlElement(name = "config")
	public String config;

	public Long getmapperID()
	{
		return mapperID;
	}

	public void setmapperID(Long mapperID)
	{
		this.mapperID = mapperID;
	}

	public String getnamespace()
	{
		return namespace;
	}

	public void setnamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public String geteventname()
	{
		return eventname;
	}

	public void seteventname(String eventname)
	{
		this.eventname = eventname;
	}

	public String getendpoint()
	{
		return endpoint;
	}

	public void setendpoint(String endpoint)
	{
		this.endpoint = endpoint;
	}

	public String getconfig()
	{
		return config;
	}

	public void setconfig(String config)
	{
		this.config = config;
	}

}
