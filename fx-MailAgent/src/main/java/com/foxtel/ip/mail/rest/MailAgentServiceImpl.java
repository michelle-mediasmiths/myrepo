package com.foxtel.ip.mail.rest;

import com.foxtel.ip.mail.process.ReadBodyFile;
import com.foxtel.ip.mail.process.ReadConfiguration;
import com.foxtel.ip.mail.variablereplacer.EMailTemplater;
import com.foxtel.ip.mailclient.MailAgentService;
import com.foxtel.ip.mailclient.ServiceCallerEntity;
import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class MailAgentServiceImpl implements MailAgentService
{
	private static final transient Logger logger = Logger.getLogger(MailAgentServiceImpl.class);

	@Inject
	protected EmailSenderService emailService;

	@Inject
	protected ReadConfiguration readConfiguration;

	@Inject
	protected ReadBodyFile readBodyFile;

	protected EMailTemplater eMailTemplater = new EMailTemplater();

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

		List<MailTemplate> templateList = readConfiguration.findTemplate(caller.eventName, caller.namespace);

		if (templateList != null)
		{
			if (logger.isInfoEnabled())
				logger.info("Number of items matching eventname and namespace: " + templateList.size());

			for (MailTemplate mailTemplate : templateList)
			{

				// This is needed so body section is not overwritten with contents of file
				MailTemplate m = new MailTemplate();
				m.setEmailaddresses(mailTemplate.getEmailaddresses());
				m.setSubject(mailTemplate.getSubject());

				m.setBody(mailTemplate.getBody());

				if (caller.payload.startsWith("%"))
				{
					if (logger.isInfoEnabled())
						logger.info("Payload is url encoded, decoding...");
					caller.payload = decoder(caller.payload);
				}

				m = eMailTemplater.generate(m, caller.payload, caller.comment);

				if (m.getEmailaddresses().getEmailaddress().size() != 0)
				{
					sendEmailsForEvent(m);
				}
				else
				{
					logger.info("No email addresses found! Check configuration");
				}
			}
		}
		else
		{
			if (logger.isInfoEnabled())
				logger.info("Nothing matching eventname and namespace");
		}
	}

	private void sendEmailsForEvent(final MailTemplate m) throws EmailException, MessagingException
	{

		for (String emailAddress : m.getEmailaddresses().getEmailaddress())
		{
			if (logger.isDebugEnabled())
				logger.debug("Preparing to send mail to: " + emailAddress);

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
