package com.mediasmiths.foxtel.ip.mail.threadmanager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mediasmiths.foxtel.ip.mail.data.db.dao.EventTableDao;
import com.mediasmiths.foxtel.ip.mail.data.db.dao.EventingTableDao;
import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventTableEntity;
import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventingTableEntity;
import com.mediasmiths.foxtel.ip.mail.rest.MailAgentServiceImpl;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import org.apache.log4j.Logger;

import java.util.List;

@Singleton
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

		int deleteCount;
		do
		{
			deleteCount = deletePage();
		}
		while (deleteCount != 0);
	}

	@Transactional
	protected int deletePage()
	{
		List<EventingTableEntity> eventing;
		eventing = eventingTableDao.getAll(0, 1000);
		for (EventingTableEntity eventingEntity : eventing)
		{
			eventingTableDao.delete(eventingEntity);
		}
		return eventing.size();
	}
}
