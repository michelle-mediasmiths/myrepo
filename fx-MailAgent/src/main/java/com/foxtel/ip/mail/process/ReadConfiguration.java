package com.foxtel.ip.mail.process;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;
import org.apache.log4j.Logger;

import java.util.List;

public class ReadConfiguration
{
	private static final transient Logger logger = Logger.getLogger(ReadConfiguration.class);

	@Inject(optional = false)
	@Named("email.configuration")
	public EventMailConfiguration findMailTemplateListFromFile;

	public ReadConfiguration()
	{
	}

	/**
	 * Finds list of mail template, based on eventname and namespace
	 * 
	 * @param eventName
	 * @param nameSpace
	 * @return
	 */
	public List<MailTemplate> findTemplate(String eventName, String nameSpace)
	{
		if (logger.isTraceEnabled())
			logger.info("ReadConfiguration Called");

		List<MailTemplate> mailTemplateList;
		try
		{
			mailTemplateList = findMailTemplateListFromFile.getTemplate(eventName, nameSpace);

			if (mailTemplateList != null)
				return mailTemplateList;
			else
				logger.info("No matches found! ");

			return null;
		}
		catch (Throwable e)
		{
			logger.error("Exception trapped : ", e);
			return null;
		}

	}
}
