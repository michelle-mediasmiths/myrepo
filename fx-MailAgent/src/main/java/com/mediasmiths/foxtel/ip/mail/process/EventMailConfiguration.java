package com.mediasmiths.foxtel.ip.mail.process;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.email.FoxtelEmailConfiguration;
import com.mediasmiths.foxtel.ip.common.email.MailData;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;




public class EventMailConfiguration
{
	private static final transient Logger logger = Logger.getLogger(EventMailConfiguration.class);

	private FoxtelEmailConfiguration mailDataList;

	/**
	 * Read configuration file to know which mail data corresponds to eventname and namespace
	 * 
	 * @param config
	 * @throws JAXBException
	 */
	public EventMailConfiguration(File config, JAXBSerialiser JAXB) throws Throwable
	{
		logger.trace("Load Email Configuration from " + config.getAbsolutePath());

		try
		{
			mailDataList = (FoxtelEmailConfiguration) JAXB.deserialise(new BufferedReader(new FileReader(config)));

			if (mailDataList == null)
				throw new Exception("Configuration file is empty");

			List<MailData> genTemplates = new ArrayList<>();

			for (MailData md : mailDataList.getMailData())
			{
				MailData genMD = new MailData();
				genMD.setEventname(md.getEventname());
				genMD.setNamespace(md.getNamespace());

				genMD.setMailTemplate(getGeneratorTemplate(md.getMailTemplate()));
				genTemplates.add(genMD);
			}
			mailDataList.getMailData().clear();
			mailDataList.getMailData().addAll(genTemplates);

			logger.info("Email templates unmarshalled from file " + config.getAbsolutePath());
		}
		catch (Throwable e)
		{
			logger.error("Unable to read configuration file " + config.getAbsolutePath(), e);

			throw e;
		}

	}

	private MailTemplate getGeneratorTemplate(final MailTemplate mt) throws Throwable
	{
        String templateClass = mt.getClazz();

		try
		{
			MailTemplate t = (MailTemplate)Class.forName(templateClass).newInstance();
			t.setBody(mt.getBody());
			t.setEmailaddresses(mt.getEmailaddresses());
			t.setSubject(mt.getSubject());
			return t;
		}
		catch (Throwable e)
		{
			logger.error("Unable to instantiate a template generator: " + templateClass, e);
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
	public List<EmailTemplateGenerator> getTemplate(String eventName, String nameSpace)
	{
		List<EmailTemplateGenerator> mailTemplateList = new ArrayList<>();

		// Reading the configuration file, this is used to find email address, title and body file location of an event using an given namespace and eventname
		if (logger.isTraceEnabled())
			logger.info("Attempting to find mailtemplate for : " + eventName + " and " + nameSpace);

		for (MailData m : mailDataList.getMailData())
		{
			System.out.println("Search " + m.getEventname() + " " + m.getNamespace());

			if (m.getEventname().equalsIgnoreCase(eventName) && m.getNamespace().equalsIgnoreCase(nameSpace))
			{
				if (logger.isTraceEnabled())
					logger.info("Found item matching: " + eventName + " and " + nameSpace);

				Object mailTemplate = m.getMailTemplate();

				System.out.println(mailTemplate.getClass().getName());

				if (mailTemplate instanceof EmailTemplateGenerator)
				{
				    mailTemplateList.add((EmailTemplateGenerator)mailTemplate);
				}
				else
				{
					System.out.println("Not a generator");
					logger.error("Email template is not a template generator: " + m.getClass().getName());
				}
			}
		}

		return mailTemplateList;
	}
}
