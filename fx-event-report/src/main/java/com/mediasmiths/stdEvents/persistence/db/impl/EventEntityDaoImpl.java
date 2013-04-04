package com.mediasmiths.stdEvents.persistence.db.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;


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

	/*@Override
	public void deleteEventing(Long id)
	{
		Eventing eventing = getById(id);
		eventingType = null;
		eventingDao.deleteById(id);
	}*/

	public ArrayList<EventEntity> toBeanArray(List<EventEntity> items)
	{	
		ArrayList<EventEntity> events = new ArrayList<EventEntity>();
		for (EventEntity event : items)
		{
			events.add(event);
		}
		return events;
	}

	@Transactional
	public List<EventEntity> namespacePaginated(String namespace, int start, int max)
	{
		logger.info("Finding events from " + start + " namespace: " + namespace + " max: " + max);
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("namespace", namespace));
		criteria.setFirstResult(start);
		criteria.setMaxResults(max);
		logger.info("Finished search");
		return getList(criteria);
	}

	@Override
	public List<EventEntity> findUniquePaginated(String namespace, String eventName, int start, int max)
	{
		logger.info("Finding events from " + start + " namespace " + namespace + " eventname: " + eventName + " max: " + max);
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("namespace", namespace));
		criteria.add(Restrictions.eq("eventName", eventName));
		criteria.setFirstResult(start);
		criteria.setMaxResults(max);
		logger.info("Finished search");
		return getList(criteria);
	}

	@Override
	public List<EventEntity> eventnamePaginated(String eventname, int start, int max)
	{
		logger.info("Finding events from " + start + " eventname: " + eventname + " max: " + max);
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("eventName", eventname));
		criteria.setFirstResult(start);
		criteria.setMaxResults(max);
		logger.info("Finished search");
		return getList(criteria);
	}


}
