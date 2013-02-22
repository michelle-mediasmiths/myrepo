package com.foxtel.ip.mail.threadmanager;

import java.util.List;

import org.apache.log4j.Logger;

import com.foxtel.ip.mail.data.db.dao.EventTableDao;
import com.foxtel.ip.mail.data.db.dao.EventingTableDao;
import com.foxtel.ip.mail.data.db.entity.EventTableEntity;
import com.foxtel.ip.mail.data.db.entity.EventingTableEntity;
import com.foxtel.ip.mail.rest.MailAgentServiceImpl;
import com.google.inject.Inject;

public class DeleteItemsIntable
{
	public static final transient Logger logger = Logger.getLogger(DeleteItemsIntable.class);
	public final EventTableDao eventTableDao;
	public final EventingTableDao eventingTableDao;
	public MailAgentServiceImpl mailAgentServiceImpl;

	@Inject
	public DeleteItemsIntable(
			EventTableDao eventTableDao,
			EventingTableDao eventingTableDao,
			MailAgentServiceImpl mailAgentServiceImpl)
	{
		this.eventTableDao = eventTableDao;
		this.eventingTableDao = eventingTableDao;
		this.mailAgentServiceImpl = mailAgentServiceImpl;

	}

	public void deleteEvent()
	{
		logger.info("Deleting all items Event table");
		List<EventTableEntity> event = eventTableDao.getAll();
		for (EventTableEntity eventEntity : event)
		{
			eventTableDao.delete(eventEntity);
		}
	}

	public void deleteEventing()
	{
		logger.info("Deleting all items Eventing table");

		List<EventingTableEntity> eventing = eventingTableDao.getAll();
		for (EventingTableEntity eventingEntity : eventing)
		{
			eventingTableDao.delete(eventingEntity);
		}
	}
}
