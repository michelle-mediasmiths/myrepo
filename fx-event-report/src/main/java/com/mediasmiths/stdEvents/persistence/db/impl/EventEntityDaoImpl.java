package com.mediasmiths.stdEvents.persistence.db.impl;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class EventEntityDaoImpl extends HibernateDao<EventEntity, Long> implements EventEntityDao
{

	private static final transient Logger logger = Logger.getLogger(EventEntityDaoImpl.class);

	public EventEntityDaoImpl()
	{
		super(EventEntity.class);
	}

	/**
	 * Returns a list of events from a requested namespace
	 * 
	 * @param namespace
	 * @return List<Event>
	 */
	@Transactional
	public List<EventEntity> findByNamespace(String namespace)
	{
		logger.info("Finding events...");
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("namespace", namespace));
		logger.info("Finished search");
		logger.info("List: " +  getList(criteria));
		return getList(criteria);
	}

	/**
	 * Returns a list of events with a requested eventName
	 * 
	 * @param eventName
	 * @return List<Event>
	 */
	@Transactional
	public List<EventEntity> findByEventName(String eventName)
	{
		logger.info("Finding events...");
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("eventName", eventName));
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
	public List<EventEntity> findUnique(String namespace, String eventName)
	{
		logger.info("finding events...");
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("namespace", namespace));
		criteria.add(Restrictions.eq("eventName", eventName));
		logger.info("Finished search");
		return getList(criteria);
	}

	@Transactional
	public void saveFile (String eventString)
	{
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("/Users/alisonboal/Documents/PersistenceInterface/sptFiles/eventFile.txt"));
			out.write(eventString);
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public List<EventEntity> findUniquePaginatedDate(String namespace, String eventname, int start, int max, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>findUniquePaginatedDate");
		logger.info("Finding events from " + start + " namespace " + namespace + " eventname: " + eventname + " max: " + max + " startDate: " + startDate + " endDate: " + endDate);;
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("namespace", namespace));
		criteria.add(Restrictions.eq("eventName", eventname));
		logger.debug("start: " + startDate.getMillis() + " end: " + endDate.getMillis());
		criteria.add(Restrictions.between("time", startDate.getMillis(), endDate.getMillis()));
		criteria.setFirstResult(start);
		criteria.setMaxResults(max);
		logger.info("finished search");
		logger.debug("<<<findUniquePaginated");	
		return getList(criteria);
	}
	
	@Override
	public List<EventEntity> eventnamePaginatedDate(String eventname, int start, int max, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>eventnamePaginatedDate");
		logger.info("Finding events from " + start + " eventname: " + eventname + " max: " + max);
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("eventName", eventname));
		logger.debug("start: " + startDate.getMillis() + " end: " + endDate.getMillis());
		criteria.add(Restrictions.between("time", startDate.getMillis(), endDate.getMillis()));
		criteria.setFirstResult(start);
		criteria.setMaxResults(max);
		logger.debug("<<<eventnamePaginated");
		return getList(criteria);
	}


	@Override
	@Transactional
	public List<EventEntity> getByNamePaged(final String eventName, final int start, final int max)
	{
		Criteria criteria = createCriteria().setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		criteria.add(Restrictions.eq("eventName", eventName));
		criteria.setFirstResult(start);
		criteria.setMaxResults(max);
		criteria.setFetchSize(max);

		return criteria.list();
	}
}
