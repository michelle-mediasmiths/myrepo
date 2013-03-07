package com.mediasmiths.foxtel.ip.mail.threadmanager;

import com.mediasmiths.foxtel.ip.mail.data.db.dao.EventTableDao;
import com.mediasmiths.foxtel.ip.mail.data.db.dao.EventingTableDao;
import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventTableEntity;
import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventingTableEntity;
import com.mediasmiths.foxtel.ip.mail.rest.MailAgentServiceImpl;
import com.foxtel.ip.mailclient.ServiceCallerEntity;
import com.google.inject.Inject;
import org.apache.log4j.Logger;

import java.util.List;

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

		List<EventingTableEntity> eventing = eventingTableDao.getAll(0, 1000);

		if (logger.isTraceEnabled()) logger.info("Entities in eventing table: " + eventing.size());

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

					try
					{
						mailAgentServiceImpl.sendMail(serviceCallerEntity);
						eventingTableDao.delete(eventEntity);
					}
					catch (Exception e)
					{
						logger.error("Failed to send email for - " + event.eventName + "..queued to resend later.");
					}
				}
				else
				{

					logger.info("Could not find event with ID: " + eventID + "...deleting as corrupt");

					eventingTableDao.delete(eventEntity);
				}
			}
			catch (Throwable t)
			{
				logger.info("Could not find event with ID: " + eventEntity.getEventId() + " ...will retry later.");

				logger.error(t);
			}
		}

	}
}
