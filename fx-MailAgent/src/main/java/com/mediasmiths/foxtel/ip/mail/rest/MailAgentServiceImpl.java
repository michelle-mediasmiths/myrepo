package com.mediasmiths.foxtel.ip.mail.rest;

import com.foxtel.ip.mailclient.MailAgentService;
import com.foxtel.ip.mailclient.ServiceCallerEntity;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.mail.process.EventMailConfiguration;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MailAgentServiceImpl implements MailAgentService
{
	private static final transient Logger logger = Logger.getLogger(MailAgentServiceImpl.class);

	private static final JAXBSerialiser JAXB = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.events");

	@Inject
	protected EmailSenderService emailService;

	@Inject
	@Named("email.configuration")
	EventMailConfiguration emailConfig;

	public MailAgentServiceImpl()
	{
	}

	@Override
	public String index()
	{
		return "Hello world! Fx-MailAgent functioning";
	}

	/**
	 * Takes in service caller entity and processes
	 */
	@Override
	public void sendMail(ServiceCallerEntity caller) throws Exception
	{

		if (logger.isInfoEnabled())
			logger.info("Preparing to send mail, MailAgentServiceImpl Called");

		EmailTemplateGenerator mailTemplate = emailConfig.getTemplate(caller.eventName, caller.namespace);

		if (mailTemplate != null)
		{
			logger.info("Email event registered for: " + caller.eventName);

			if (caller.payload.startsWith("%"))
			{
				if (logger.isInfoEnabled())
					logger.info("Payload is url encoded, decoding...");

				caller.payload = decoder(caller.payload);
			}

			MailTemplate m = getMailTemplate(caller, mailTemplate);

			if (m.getEmailaddresses().getEmailaddress().size() != 0)
			{
				sendEmailsForEvent(m);
			}
			else
			{
				logger.info("No email addresses found! Check configuration");
			}

		}
		else
		{
			if (logger.isInfoEnabled())
				logger.info("Nothing matching eventname and namespace");
		}
	}

	private MailTemplate getMailTemplate(final ServiceCallerEntity caller, final EmailTemplateGenerator mailTemplate)
	{
		try
		{
			Object payloadObj = JAXB.deserialise(caller.payload);

			if (mailTemplate.handles(payloadObj))
			{
			    return mailTemplate.customiseTemplate(caller.payload, caller.comment);
			}
			else
			{
				logger.error("Template does not handle type .. sending reduced functionality email." + caller.eventName);

				return defaultTemplate(caller.payload, (MailTemplate)mailTemplate);
			}
		}
		catch (Exception e)
		{
			logger.error("Unable to deserialise obj .. sendind reduced functionality email.", e);
			return defaultTemplate(caller.payload, (MailTemplate)mailTemplate);
		}
	}

	/**
	 *
	 * @param payload
	 * @param mailTemplate
	 * @return a template formed just from the raw title, subject, body
	 */
	private MailTemplate defaultTemplate(final String payload, final MailTemplate mailTemplate)
	{
		MailTemplate t = new MailTemplate();
		t.setEmailaddresses(mailTemplate.getEmailaddresses());
		t.setSubject(mailTemplate.getSubject());
		t.setBody(t.getBody() + payload);
		return t;
	}

	private void sendEmailsForEvent(final MailTemplate m) throws EmailException, MessagingException
	{

		for (String emailAddress : m.getEmailaddresses().getEmailaddress())
		{
			if (logger.isDebugEnabled())
				logger.debug("Preparing to send mail to: " + emailAddress);

			logger.info("Sending email to: " + emailAddress + " with Subject: " + m.getSubject());
			try
			{

				if (m.getSubject().equals("Email Error: Could not find generator"))
				{
					// Sending normal email so unprocessed xml can be viewed
					logger.info("Could not find generator, sending normal email.");

					emailService.createEmail(emailAddress, m.getSubject(), m.getBody());
				}
				else
				{
					emailService.createMimeEmail(emailAddress, m.getSubject(), m.getBody());

					// emailService.createEmail(email, m.getSubject(), m.getBody());
				}
				if (logger.isInfoEnabled())
				{
					logger.info("Sent email to: " + emailAddress);
				}
			}
			catch (EmailException e)
			{
				logger.error("EmailException: " + e);
				throw e;

			}
			catch (MessagingException e)
			{
				logger.error("MessagingException (is your configuration right?): " + e);
				throw e;
			}

		}
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
