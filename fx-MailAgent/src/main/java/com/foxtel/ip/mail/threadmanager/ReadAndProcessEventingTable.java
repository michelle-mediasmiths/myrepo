package com.foxtel.ip.mail.threadmanager;

import java.util.List;
import org.apache.log4j.Logger;

import com.foxtel.ip.mail.data.db.dao.EventTableDao;
import com.foxtel.ip.mail.data.db.dao.EventingTableDao;
import com.foxtel.ip.mail.data.db.entity.EventTableEntity;
import com.foxtel.ip.mail.data.db.entity.EventingTableEntity;
import com.foxtel.ip.mail.rest.MailAgentServiceImpl;
import com.foxtel.ip.mailclient.ServiceCallerEntity;
import com.google.inject.Inject;

public class ReadAndProcessEventingTable
{
	public static final transient Logger logger = Logger.getLogger(ReadAndProcessEventingTable.class);
	public final EventTableDao eventTableDao;
	public final EventingTableDao eventingTableDao;
	public MailAgentServiceImpl mailAgentServiceImpl;

	@Inject
	public ReadAndProcessEventingTable(
			EventTableDao eventTableDao,
			EventingTableDao eventingTableDao,
			MailAgentServiceImpl mailAgentServiceImpl)
	{
		this.eventTableDao = eventTableDao;
		this.eventingTableDao = eventingTableDao;
		this.mailAgentServiceImpl = mailAgentServiceImpl;

	}

	/**
	 * Reads items in eventing table, find corresponding data in events table and sends to mailagentservice
	 */
	public void processEventList()
	{
		if (logger.isTraceEnabled())
			logger.info("ReadAndProcessEventingTable called: ");

		String emailStatus = "Error";
		List<EventingTableEntity> eventing = eventingTableDao.getAll();
		logger.info("Entities in eventing table: " + eventing.size());

		for (EventingTableEntity eventEntity : eventing)
		{
			try
			{
				Long eventID = eventEntity.getEventId();
				if (logger.isTraceEnabled())
					logger.info("eventTableID and eventingTableID Id: " + eventID);
				EventTableEntity event = eventTableDao.getById(eventID);

				if (event != null)
				{

					ServiceCallerEntity serviceCallerEntity = new ServiceCallerEntity();
					serviceCallerEntity.eventName = event.eventName;
					serviceCallerEntity.payload = event.payload;
					serviceCallerEntity.namespace = event.namespace;
					logger.info("Calling mail agent service.");

					emailStatus = mailAgentServiceImpl.sendMail(serviceCallerEntity);
				}

				else
				{
					if (logger.isTraceEnabled())
					{
						logger.info("Could not find event with ID: " + eventID);
					}
				}

				if (logger.isTraceEnabled())
					logger.info("Email status: " + emailStatus);

				if (!emailStatus.startsWith("Error"))
				{
					if (logger.isTraceEnabled())
						logger.info("Deleting item in eventing with Id: " + eventID);
					eventingTableDao.delete(eventEntity);
				}
				else
				{
					if (logger.isTraceEnabled())
						logger.info("item returned an error, not being deleted from table");
				}
				if (logger.isTraceEnabled())
					logger.info("No items found in event table that correspond to event, going to sleep");
			}
			catch (Throwable t)
			{
				logger.error(t);
			}
		}

	}
}
