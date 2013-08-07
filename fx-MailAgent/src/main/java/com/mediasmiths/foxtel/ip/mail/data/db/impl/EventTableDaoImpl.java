package com.mediasmiths.foxtel.ip.mail.data.db.impl;

import com.mediasmiths.foxtel.ip.mail.data.db.dao.EventTableDao;
import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventTableEntity;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class EventTableDaoImpl extends HibernateDao<EventTableEntity, Long> implements EventTableDao
{
	private static final transient Logger logger = Logger.getLogger(EventTableDaoImpl.class);

	/**
	 * Returns a list of events from a requested namespace
	 * 
	 * @param namespace
	 * @return List<Event>
	 */
	@Transactional
	public List<EventTableEntity> findByNamespace(String namespace)
	{
		if (logger.isTraceEnabled())
			logger.info("Finding events...");
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("namespace", namespace));
		if (logger.isTraceEnabled())
			logger.info("Finished search");
		return getList(criteria);
	}

	/**
	 * Returns a list of events with a requested eventName
	 * 
	 * @param eventName
	 * @return List<Event>
	 */
	@Transactional
	public List<EventTableEntity> findByEventName(String eventName)
	{
		if (logger.isTraceEnabled())
			logger.info("Finding events...");
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("eventName", eventName));
		if (logger.isTraceEnabled())
			logger.info("Finished search");
		return getList(criteria);
	}

	/**
	 * Returns a unique event with a specific namespace and eventName
	 * 
	 * @param namespace
	 * @param eventName
	 * @return Event
	 */
	@Transactional
	public List<EventTableEntity> findUnique(String namespace, String eventName)
	{

		if (logger.isTraceEnabled())
			logger.info("finding events...");
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("namespace", namespace));
		criteria.add(Restrictions.eq("eventName", eventName));
		if (logger.isTraceEnabled())
			logger.info("Finished search");
		return getList(criteria);
	}

	@Transactional
	public EventTableEntity getFirstID()
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
