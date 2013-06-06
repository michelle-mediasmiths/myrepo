package com.mediasmiths.foxtel.ip.mail.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mediasmiths.foxtel.ip.mail.process.EventMailConfiguration;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.inject.Named;
import java.io.File;

public class EventMailConfigurationProvider implements Provider<EventMailConfiguration>
{

	private final static Logger logger = Logger.getLogger(EventMailConfiguration.class);

	@Inject
	@Named("mail.agent.configuration.location")
	private String configLocation;

	@Inject
	@Named("mail.agent.email.config.reload.interval")
	private Integer reloadInterval;

	private EventMailConfiguration lastKnownConfig = null;
	private DateTime configLastLoaded = DateTime.now();

	private final JAXBSerialiser jaxb = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.email");


	@Override
	public EventMailConfiguration get()
	{
		if (lastKnownConfig == null || DateTime.now().isAfter(configLastLoaded.plusMillis(reloadInterval)))
		{
			logger.info("Looking for config file: " + configLocation);

			File configFile = new File(configLocation);
			if (!configFile.exists() || !configFile.canRead())
			{

				logger.error("Config file does not exist or is unreadable " + configLocation);
				throw new RuntimeException("Configuration file does not exist or is unreadable " +
				                           configLocation +
				                           " looking in " +
				                           configFile.getAbsolutePath());
			}

			try
			{
				EventMailConfiguration findMailTemplateListFromFile = new EventMailConfiguration(configFile, jaxb);
				logger.debug("EventMailConfiguration loaded successfully!");

				configLastLoaded = DateTime.now();
				lastKnownConfig = findMailTemplateListFromFile;
				return findMailTemplateListFromFile;
			}
			catch (Throwable t)
			{
				logger.error("Error loading configuration", t);
				throw new RuntimeException("Error loading config from " + configLocation, t);
			}
		}
		else
		{
			logger.debug("Config loaded too recently, returning previously known value");
			return lastKnownConfig;
		}
	}
}
