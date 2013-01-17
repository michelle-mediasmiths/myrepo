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

	@Override
	public void printXML(List<EventEntity> events)
	{
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("EventReport");
			Attr attr = doc.createAttribute("date");
			attr.setValue(new Date().toString());
			rootElement.setAttributeNode(attr);
			doc.appendChild(rootElement);
			
			for (int i=0; i<events.size(); i++) {
				EventEntity event = events.get(i);
				
				Element eventEntity = doc.createElement("eventEntity");
				rootElement.appendChild(eventEntity);
				
				Element time = doc.createElement("time");
				time.appendChild(doc.createTextNode(String.valueOf(event.getTime())));
				eventEntity.appendChild(time);
				
				Element namespace = doc.createElement("namespace");
				namespace.appendChild(doc.createTextNode(event.getNamespace()));
				eventEntity.appendChild(namespace);
				
				Element eventName = doc.createElement("eventName");
				eventName.appendChild(doc.createTextNode(event.getEventName()));
				eventEntity.appendChild(eventName);
				
				Element payload = doc.createElement("payload");
				payload.appendChild(doc.createTextNode(event.getPayload()));
				eventEntity.appendChild(payload);
				
				Element content = doc.createElement("content");
				payload.appendChild(doc.createTextNode(event.getContent()));
				eventEntity.appendChild(content);
				
				Element eventTime = doc.createElement("eventTime");
				payload.appendChild(doc.createTextNode(String.valueOf(event.getEventTime())));
				eventEntity.appendChild(eventTime);
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("/Users/alisonboal/Documents/PersistenceInterface/sptFiles/report.xml"));
			
			transformer.transform(source, result);
			logger.info("File saved");
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}catch (TransformerException e) {
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<EventEntity> toBeanArray(List<EventEntity> items)
	{	
		ArrayList<EventEntity> events = new ArrayList<EventEntity>();
		for (EventEntity event : items)
		{
			events.add(event);
		}
		return events;
	}

		
}
