package com.foxtel.ip.mail.guice;

import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.foxtel.ip.mail.data.FileAndFolderLocations;
import com.foxtel.ip.mail.data.db.dao.EventTableDao;
import com.foxtel.ip.mail.data.db.dao.EventingTableDao;
import com.foxtel.ip.mail.data.db.impl.EventTableDaoImpl;
import com.foxtel.ip.mail.data.db.impl.EventingTableDaoImpl;
import com.foxtel.ip.mail.process.FindMailTemplateListFromFile;
import com.foxtel.ip.mail.process.ReadBodyFile;
import com.foxtel.ip.mail.process.ReadConfiguration;
import com.foxtel.ip.mail.rest.EmailSenderService;
import com.foxtel.ip.mail.rest.EmailSenderServiceImpl;
import com.foxtel.ip.mail.rest.MailAgentServiceImpl;
import com.foxtel.ip.mail.threadmanager.DeleteItemsIntable;
import com.foxtel.ip.mail.threadmanager.EventMapperThreadManager;
import com.foxtel.ip.mail.threadmanager.ReadAndProcessEventingTable;
import com.foxtel.ip.mailclient.MailAgentService;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

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
		bind(ReadConfiguration.class).asEagerSingleton();
		bind(ReadBodyFile.class).asEagerSingleton();
		bind(DeleteItemsIntable.class).asEagerSingleton();

	}

	@Provides
	@Named("email.configuration")
	public FindMailTemplateListFromFile provideEmailConfig(FileAndFolderLocations fileLocation) throws JAXBException
	{
		logger.info("Creating FindMailTemplateListFromFile");
		logger.info("Looking for config file: " + fileLocation.configurationLocation);

		try
		{
			FindMailTemplateListFromFile findMailTemplateListFromFile = new FindMailTemplateListFromFile(
					fileLocation.configurationLocation);
			logger.info("FindMailTemplateListFromFile loaded successfully!");

			return findMailTemplateListFromFile;
		}
		catch (JAXBException e)
		{
			logger.error("Unable to read configuration file: " + fileLocation.configurationLocation, e);
			throw e;
		}

	}

}
