package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jfree.util.Log;

import com.mediasmiths.foxtel.ip.common.events.CreateOrUpdateTitle;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
import com.mediasmiths.stdEvents.persistence.db.dao.TitleDao;

public class TitleDaoImpl extends HibernateDao<Title, String> implements TitleDao
{

	private final static Logger log = Logger.getLogger(TitleDaoImpl.class);
	
	protected JAXBSerialiser serialiser = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);

	public TitleDaoImpl()
	{
		super(Title.class);
	}

	@Override
	@Transactional
	public void createOrUpdateTitle(EventEntity event)
	{
		CreateOrUpdateTitle cout = (CreateOrUpdateTitle) serialiser.deserialise(event.getPayload());

		String id = cout.getTitleID();
		String title = cout.getTitle();
		List<String> channels = Arrays.asList(StringUtils.split(cout.getChannels(), ','));

		Title theTitle = new Title();
		theTitle.setTitleId(id);
		theTitle.setChannels(channels);
		theTitle.setTitle(title);

		log.debug(String.format("Save or update title information for reports id %s title %s channels %s", id,title,cout.getChannels()));
		
		saveOrUpdate(theTitle);
	}

}
