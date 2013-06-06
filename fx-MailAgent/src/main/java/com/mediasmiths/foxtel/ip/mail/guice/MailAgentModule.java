package com.mediasmiths.foxtel.ip.mail.guice;

import com.foxtel.ip.mailclient.MailAgentService;
import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.ip.mail.data.db.dao.EventTableDao;
import com.mediasmiths.foxtel.ip.mail.data.db.dao.EventingTableDao;
import com.mediasmiths.foxtel.ip.mail.data.db.impl.EventTableDaoImpl;
import com.mediasmiths.foxtel.ip.mail.data.db.impl.EventingTableDaoImpl;
import com.mediasmiths.foxtel.ip.mail.process.EventMailConfiguration;
import com.mediasmiths.foxtel.ip.mail.rest.EmailSenderService;
import com.mediasmiths.foxtel.ip.mail.rest.EmailSenderServiceImpl;
import com.mediasmiths.foxtel.ip.mail.rest.MailAgentServiceImpl;
import com.mediasmiths.foxtel.ip.mail.threadmanager.DeleteItemsIntable;
import com.mediasmiths.foxtel.ip.mail.threadmanager.EventMapperThreadManager;
import com.mediasmiths.foxtel.ip.mail.threadmanager.ReadAndProcessEventingTable;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;
import org.apache.log4j.Logger;

public class MailAgentModule extends AbstractModule
{
	private static final transient Logger logger = Logger.getLogger(MailAgentModule.class);

	@Override
	protected void configure()
	{
		// Rest Service
		bind(MailAgentService.class).to(MailAgentServiceImpl.class);
		RestResourceRegistry.register(MailAgentService.class);
		bind(EmailSenderService.class).to(EmailSenderServiceImpl.class);

		// Table Data
		bind(EventTableDao.class).to(EventTableDaoImpl.class);
		bind(EventingTableDao.class).to(EventingTableDaoImpl.class);

		bind(EventMapperThreadManager.class).asEagerSingleton();
		bind(ReadAndProcessEventingTable.class).asEagerSingleton();
		bind(DeleteItemsIntable.class).asEagerSingleton();
		bind(EventMailConfiguration.class).toProvider(EventMailConfigurationProvider.class);

	}
}
