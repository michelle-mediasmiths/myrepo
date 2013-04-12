package com.mediasmiths.foxtel.ip.mail.rest;

import com.foxtel.ip.mailclient.MailAgentService;
import com.foxtel.ip.mailclient.ServiceCallerEntity;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.mail.process.EventMailConfiguration;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.mail.thymeleaf.ThymeleafServiceSample;

import org.apache.log4j.Logger;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

//This class finds any matching mail templates for eventname and namespace.It also decodes and encoded payloads.
public class MailAgentServiceImpl implements MailAgentService
{
	private static final transient Logger logger = Logger.getLogger(MailAgentServiceImpl.class);

	@Inject
	protected EmailSenderService emailService;
	
	@Inject
	protected ThymeleafServiceSample thymeleafServiceSample;

	@Inject
	@Named("email.configuration")
	EventMailConfiguration emailConfig;


	@Override
	public String index()
	{
		return "Hello world! Fx-MailAgent functioning";
	}

	/**
	 * Takes in service caller entity and processes, checks if anything matches namespace and eventname
	 */
	@Override
	public void sendMail(ServiceCallerEntity caller) throws Exception
	{

		if (logger.isInfoEnabled())
			logger.info("Preparing to send mail, MailAgentServiceImpl Called");

		EmailTemplateGenerator emailTemplateGenerator = emailConfig.getTemplate(caller.eventName, caller.namespace);

		if (emailTemplateGenerator != null)
		{
			logger.info("Email event registered for: " + caller.eventName);
			caller.setPayload(objXMLFor(caller));
			thymeleafServiceSample.createMailTemplate(caller, emailTemplateGenerator);
		}
		else
		{
			if (logger.isInfoEnabled())
				logger.info("Nothing matching eventname and namespace");
		}
	}

	private String objXMLFor(final ServiceCallerEntity caller)
	{
		if(caller.payload.startsWith("%"))
		{
			if (logger.isInfoEnabled())
				logger.info("Payload is url encoded, decoding...");

			caller.payload = decoder(caller.payload);
		}

		return caller.payload.replaceFirst("^ *", "");
	}

	/**
	 * Some payloads may be url encoded, decodes them if they are
	 * 
	 * @param encodedString
	 * @return
	 */
	public String decoder(String encodedString)
	{
		if (logger.isInfoEnabled())
			logger.info("Decoder called, attempting to decode: " + encodedString);
		try
		{
			return URLDecoder.decode(encodedString, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			logger.error(e);
		}
		return "Error decoding xml";

	}

}
