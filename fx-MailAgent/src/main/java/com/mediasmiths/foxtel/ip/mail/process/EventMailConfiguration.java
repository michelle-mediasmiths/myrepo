package com.mediasmiths.foxtel.ip.mail.process;

import com.mediasmiths.foxtel.ip.common.email.FoxtelEmailConfiguration;
import com.mediasmiths.foxtel.ip.common.email.MailData;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;


public class EventMailConfiguration
{
	private static final transient Logger logger = Logger.getLogger(EventMailConfiguration.class);


	private Map<String, MailData> eventMapping = new HashMap<>();

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
			FoxtelEmailConfiguration mailDataList = (FoxtelEmailConfiguration) JAXB.deserialise(new BufferedReader(new FileReader(config)));

			if (mailDataList == null)
				throw new Exception("Configuration file is empty");

			for (MailData md : mailDataList.getMailData())
			{
				MailData genMD = new MailData();
				genMD.setEventname(md.getEventname());
				genMD.setNamespace(md.getNamespace());

				genMD.setMailTemplate(getGeneratorTemplate(md.getMailTemplate()));
				eventMapping.put(hashFor(md.getNamespace(), md.getEventname()), genMD);
			}

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
	public EmailTemplateGenerator getTemplate(String eventName, String nameSpace)
	{
		if (logger.isInfoEnabled()){
			logger.info(String.format("Attempting to find mailtemplate for event with name : \"%s\" and namespace \"%s", eventName,nameSpace));
		}

		MailData m = eventMapping.get(hashFor(nameSpace, eventName));

		if (m != null)
		{
			Object mailTemplate = m.getMailTemplate();

			System.out.println(mailTemplate.getClass().getName());

			if (mailTemplate instanceof EmailTemplateGenerator)
			{
				return (EmailTemplateGenerator) mailTemplate;
			}
			else
			{
				logger.error("Email template is not a template generator: " + m.getClass().getName());
				return null;
			}
		}
		else
		{
			return null;
		}


	}

	private String hashFor(final String namespace, final String eventname)
	{
		return namespace+eventname;
	}

}
