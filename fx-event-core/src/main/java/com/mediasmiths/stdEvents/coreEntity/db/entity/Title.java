package com.mediasmiths.stdEvents.coreEntity.db.entity;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Title")
public class Title
{
	@Id
	private String titleId;
	
	@Basic
	private String title;
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="TitleChannels")
	private List<String> channels;

	public String getTitleId()
	{
		return titleId;
	}

	public void setTitleId(String titleId)
	{
		this.titleId = titleId;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public List<String> getChannels()
	{
		return channels;
	}

	public void setChannels(List<String> channels)
	{
		this.channels = channels;
	}
	
}
