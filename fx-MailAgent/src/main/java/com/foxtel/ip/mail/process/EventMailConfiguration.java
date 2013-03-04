package com.foxtel.ip.mail.process;

import com.mediasmiths.foxtel.ip.common.events.FoxtelEmailConfiguration;
import com.mediasmiths.foxtel.ip.common.events.MailData;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;




public class EventMailConfiguration
{
	private static final transient Logger logger = Logger.getLogger(EventMailConfiguration.class);

	private final FoxtelEmailConfiguration mailDataList;

	/**
	 * Read configuration file to know which mail data corresponds to eventname and namespace
	 * 
	 * @param configFilePath
	 * @throws JAXBException
	 */
	public EventMailConfiguration(String configFilePath) throws JAXBException
	{
		logger.trace("EventMailConfiguration loader Called");

		File file = new File(configFilePath);

		JAXBContext jaxbContext;
		try
		{
			logger.trace("file");

			jaxbContext = JAXBContext.newInstance("com.mediasmiths.foxtel.ip.common.events");
					logger.trace("newInstance");
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			logger.trace("jaxbUnmarshaller");

			mailDataList = (FoxtelEmailConfiguration) jaxbUnmarshaller.unmarshal(file);
		}
		catch (JAXBException e)
		{
			logger.error("Unable to read configuration file " + configFilePath, e);
			throw e;
		}

	}

	/**
	 * Reading from file in resources and returns list
	 * 
	 * @param eventName
	 * @param nameSpace
	 * @return
	 */
	public List<MailTemplate> getTemplate(String eventName, String nameSpace)
	{
		List<MailTemplate> mailTemplateList = new ArrayList<MailTemplate>();

		// Reading the configuration file, this is used to find email address, title and body file location of an event using an given namespace and eventname
		if (logger.isTraceEnabled())
			logger.info("Attempting to find mailtemplate for : " + eventName + " and " + nameSpace);

		for (MailData m : mailDataList.getMailData())
		{
			if (m.getEventname().equals(eventName) && m.getNamespace().equals(nameSpace))
			{
				if (logger.isTraceEnabled())
					logger.info("Found item matching: " + eventName + " and " + nameSpace);
				mailTemplateList.add(m.getMailTemplate());
			}
		}

		if (mailTemplateList.size() > 0)
			return mailTemplateList;
		else
		{
			logger.info("No matches found, returning null");

			return null;

		}

	}
}
