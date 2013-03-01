package com.foxtel.ip.mail.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import com.foxtel.ip.mail.process.ReadBodyFile;
import com.foxtel.ip.mail.process.ReadConfiguration;
import com.foxtel.ip.mail.variablereplacer.VariableReplacer;
import com.foxtel.ip.mailclient.MailAgentService;
import com.foxtel.ip.mailclient.ServiceCallerEntity;
import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class MailAgentServiceImpl implements MailAgentService
{
	private static final transient Logger logger = Logger.getLogger(MailAgentServiceImpl.class);

	@Inject
	protected EmailSenderService emailService;

	@Inject
	protected ReadConfiguration readConfiguration;

	@Inject
	protected ReadBodyFile readBodyFile;

	protected VariableReplacer variableReplacer = new VariableReplacer();

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
	public String sendMail(ServiceCallerEntity caller)
	{

		if (logger.isInfoEnabled())
			logger.info("Preparing to send mail, MailAgentServiceImpl Called");

		List<MailTemplate> templateList = readConfiguration.findTemplate(caller.eventName, caller.namespace);

		if (templateList != null)
		{
			if (logger.isInfoEnabled())
				logger.info("Number of items matching eventname and namespace: " + templateList.size());

			for (MailTemplate test : templateList)
			{

				// This is needed so body section is not overwritten with contents of file
				MailTemplate m = new MailTemplate();
				m.setEmailaddresses(test.getEmailaddresses());
				m.setSubject(test.getSubject());

				m.setBody(readBodyFile.readFile(test.getBody()));

				if (caller.payload.startsWith("%"))
				{
					if (logger.isInfoEnabled())
						logger.info("Payload is url encoded, decoding...");
					caller.payload = decoder(caller.payload);
				}

				if (logger.isInfoEnabled())
					logger.info("Calling variable replacer");

				m = variableReplacer.generate(m, caller.payload, caller.comment);
				logger.info("variableReplacer returned");

				if (m.getEmailaddresses().getEmailaddress().size() != 0)
				{
					if (logger.isTraceEnabled())
						logger.info("Email addresses found!");
					for (String email : m.getEmailaddresses().getEmailaddress())
					{
						if (logger.isDebugEnabled())
							logger.debug("Preparing to send mail to: " + email);

						try
						{
							if (logger.isTraceEnabled())
								logger.info("Preparing to send mail to: " + email);

							if (m.getSubject().equals("Email Error: Could not find generator"))
							{
								// Sending normal email so unprocessed xml can be viewed
								logger.info("Could not find generator, sending normal email.");
								emailService.createEmail(email, m.getSubject(), m.getBody());
							}
							else
							{

//								logger.info("sending as simple (non mime email) ");
								emailService.createMimeEmail(email, m.getSubject(), m.getBody());
								// emailService.createEmail(email, m.getSubject(), m.getBody());

							}
						}
						catch (EmailException e)
						{
							logger.error("EmailException: " + e);
							return "Error: EmailException: " + e;

						}
						catch (MessagingException e)
						{
							logger.error("MessagingException (is your configuration right?): " + e);
							return "Error: MessagingException: " + e;
						}

						if (logger.isInfoEnabled())
						{
							logger.info("Sent email to: " + email);
						}
					}
				}
				else
				{
					if (logger.isTraceEnabled())
						logger.info("No email addresses found! Check configuration");
					return "No email addresses found! Check configuration";
				}
				return "Email(s) send correctly)";

			}

		}
		else
		{
			if (logger.isInfoEnabled())
				logger.info("Nothing matching eventname and namespace");
			return "No results found";
		}
		return "Error: Undefined";
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
