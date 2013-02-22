package com.foxtel.ip.mail.variablereplacer.variablegenerators;

import com.mediasmiths.foxtel.ip.common.events.EmailConfiguration;

public class EmailConfigGenerator
{

	public EmailConfiguration getConfigurationDetails(Object obj)
	{

		return getConfigClass((EmailConfiguration) obj);
	}

	EmailConfiguration getConfigClass(EmailConfiguration obj)
	{

		EmailConfiguration config = new EmailConfiguration();

		config.setBody(obj.getBody());
		config.setEmailaddresses(obj.getEmailaddresses());
		config.setTitle(obj.getTitle());

		return config;
	}

}
