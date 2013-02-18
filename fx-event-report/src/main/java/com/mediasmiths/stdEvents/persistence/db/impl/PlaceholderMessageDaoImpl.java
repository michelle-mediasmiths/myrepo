package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.PlaceholderMessage;
import com.mediasmiths.stdEvents.persistence.db.dao.QueryDao;

@Singleton
public class PlaceholderMessageDaoImpl extends HibernateDao<PlaceholderMessage, Long> implements EventMarshaller, QueryDao<PlaceholderMessage>
{
	public static final transient Logger logger = Logger.getLogger(PlaceholderMessageDaoImpl.class);

	public PlaceholderMessageDaoImpl()
	{
		super(PlaceholderMessage.class);
	}

	@Transactional
	public void save(EventEntity event)
	{
		PlaceholderMessage message = get(event);
		message.event = event;
		logger.info("Saving message");
		saveOrUpdate(message);
	}
	
	@Transactional
	public PlaceholderMessage get(EventEntity event)
	{
		logger.info("Setting placeholder message for event");
		PlaceholderMessage message =  new PlaceholderMessage();
		String messageString = event.getPayload();
		if (messageString.contains("Invalid"))
		{
			logger.info("Invalid message detected");
			message.setActions(messageString);
		}
		else if (event.getEventName().equals("failed"))
		{
			logger.info("Placeholder message type = failed");
			message.setActions(event.getPayload());
		}
		else
		{
			if (messageString.contains("Actions>"))
				message.setActions(messageString.substring(messageString.indexOf("Actions")+8, messageString.indexOf("/Actions")));
			if (messageString.contains("senderID"))
				message.setSenderID(messageString.substring(messageString.indexOf("senderID")+9, messageString.indexOf(' ', messageString.indexOf("senderID"))));
			if (messageString.contains("messageID"))
				message.setMessageID(messageString.substring(messageString.indexOf("messageID")+10, messageString.indexOf('>', messageString.indexOf("messageID"))));
		}
		logger.info("Placeholder message constructed");
		return message;
	}
	
	@Transactional
	public List<PlaceholderMessage> getAll()
	{
		logger.info("Getting all placeholder messages...");
		List<PlaceholderMessage> messages = super.getAll();
		logger.info("Finished search");
		return messages;
	}

	@Override
	public String getNamespace(EventEntity event)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo(EventEntity event)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
