package com.mediasmiths.stdEvents.persistence.db.impl;

import org.apache.log4j.Logger;

import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.events.db.entity.EventingEntity;
import com.mediasmiths.stdEvents.persistence.db.dao.EventingDao;

public class EventingDaoImpl extends HibernateDao<EventingEntity, Long> implements EventingDao
{
	public static final transient Logger logger = Logger.getLogger(EventingDaoImpl.class);

	public EventingDaoImpl()
	{
		super(EventingEntity.class);
	}
}
