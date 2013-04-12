package com.mediasmiths.foxtel.ip.mail.guice;

import com.foxtel.ip.mailclient.MailAgentService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
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
import com.mediasmiths.foxtel.ip.mail.thymeleaf.ThymeleafSample;
import com.mediasmiths.foxtel.ip.mail.thymeleaf.ThymeleafSampleImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;

import javax.inject.Named;
import java.io.File;

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

		
		RestResourceRegistry.register(ThymeleafSample.class);
		bind(ThymeleafSample.class).to(ThymeleafSampleImpl.class);
		
		
		// Table Data
		bind(EventTableDao.class).to(EventTableDaoImpl.class);
		bind(EventingTableDao.class).to(EventingTableDaoImpl.class);

		bind(EventMapperThreadManager.class).asEagerSingleton();
		bind(ReadAndProcessEventingTable.class).asEagerSingleton();
		bind(DeleteItemsIntable.class).asEagerSingleton();

	}

	@Provides
	@Named("email.configuration")
	public EventMailConfiguration provideEmailConfig(@Named("mail.agent.configuration.location")String configLocation) throws Throwable
	{
		logger.info("Creating EventMailConfiguration");

		logger.info("Looking for config file: " + configLocation);

		try
		{
			JAXBSerialiser JAXB = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.email");

			File configFile = new File(configLocation);
			if (!configFile.exists() || !configFile.canRead())
				throw new Exception("Configuration file does not exist or is unreadable " + configLocation + " looking in " + configFile.getAbsolutePath());

			EventMailConfiguration findMailTemplateListFromFile = new EventMailConfiguration(configFile, JAXB);

			logger.info("EventMailConfiguration loaded successfully!");

			return findMailTemplateListFromFile;
		}
		catch (Throwable e)
		{
			logger.error("Unable to read configuration file: " + configLocation, e);
			throw e;
		}

	}

}
