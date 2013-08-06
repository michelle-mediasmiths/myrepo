package com.mediasmiths.foxtel.ip.mail.data.db.impl;

import com.mediasmiths.foxtel.ip.mail.data.db.dao.EventingTableDao;
import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventingTableEntity;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class EventingTableDaoImpl extends HibernateDao<EventingTableEntity, Long> implements EventingTableDao
{
	private static final transient Logger logger = Logger.getLogger(EventTableDaoImpl.class);


	@Override
	public List<EventingTableEntity> findByNamespace(String namespace)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EventingTableEntity> findByEventName(String eventName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EventingTableEntity> findUnique(String namespace, String eventName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional
	public EventingTableEntity getFirstID()
	{

		if (logger.isTraceEnabled())
			logger.info("finding events...");
		Criteria criteria = createCriteria();

		criteria.addOrder(Order.asc("id"));
		criteria.setMaxResults(1);
		criteria.add(Restrictions.like("id", "%"));
		return uniqueResult(criteria);

	}
}
