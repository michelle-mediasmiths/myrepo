package com.mediasmiths.foxtel.ip.mail.thymeleaf;

import javax.mail.MessagingException;

import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import com.foxtel.ip.mailclient.ServiceCallerEntity;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.email.Emailaddress;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.mail.process.EventMailConfiguration;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class ThymeleafServiceSample
{
	private static final transient Logger logger = Logger.getLogger(ThymeleafServiceSample.class);
	private final JAXBSerialiser JAXB;

	@Inject
	@Named("email.configuration")
	EventMailConfiguration emailConfig;

	@Inject
	private ThymeleafTemplater templater;
	
	public ThymeleafServiceSample() throws Exception
	{
		try
		{
			JAXB = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.events");
			logger.info("JAXB serialiser created:");
		}
		catch (Exception e)
		{
			logger.info("Unable to create JAXB serialiser: ", e);
			throw e;
		}
	}

	//Creates mail template for given servicecallerentity
	public void createMailTemplate(ServiceCallerEntity caller, EmailTemplateGenerator emailTemplateGenerator) throws Exception
	{
		
		logger.info("createMailTemplate called, checking for templater");

		MailTemplate m = getMailTemplate(caller, emailTemplateGenerator);

		if (m.getEmailaddresses().getEmailaddress().size() != 0)
		{
			sendEmailsForEvent(m);
		}
		else
		{
			logger.info("No email addresses found! Check configuration");
		}
	}

	
	private MailTemplate getMailTemplate(final ServiceCallerEntity caller, final EmailTemplateGenerator mailTemplate)
	{
		try
		{
			Object payloadObj = JAXB.deserialise(caller.payload);

			if (mailTemplate.handles(payloadObj))
			{
				//Eventname corresponds to the name of the template.
				return mailTemplate.customiseTemplate(payloadObj, caller.comment, caller.eventName, templater);
			}
			else
			{
				logger.error("Template does not handle type... sending reduced functionality email." + caller.eventName);
				return defaultTemplate(caller.payload, (MailTemplate) mailTemplate);
			}
		}
		catch (Exception e)
		{
			logger.error("Unable to deserialise obj .. sending reduced functionality email.", e);
			logger.info(caller.payload);
			return defaultTemplate(caller.payload, (MailTemplate) mailTemplate);
		}
	}

	private void sendEmailsForEvent(final MailTemplate m) throws EmailException, MessagingException
	{
		for (Emailaddress emailAddress : m.getEmailaddresses().getEmailaddress())
		{
			if (logger.isDebugEnabled())
				logger.debug("Preparing to send mail to: " + emailAddress);

			logger.info("Sending email to: " + emailAddress + " with Subject: " + m.getSubject());
			// try
			// {

			if (m.getSubject().equals("Email Error: Could not find generator"))
			{
				// Sending normal email so unprocessed xml can be viewed
				logger.info("Could not find generator, sending normal email.");

				// emailService.createEmail(emailAddress.getValue(), m.getSubject(), m.getBody(), null);
			}
			else
			{
				if (m.getFileAttachments() == null || m.getFileAttachments().isEmpty())
				{
					logger.info("Using ThymeleafEmailSender.");

					// TODO Remove
					ThymeleafEmailSender thymeleafEmailSender = new ThymeleafEmailSender();
					thymeleafEmailSender.sendThymeleafEmail(emailAddress.getValue(), m.getSubject(), m.getBody());
				}
				else
				{
					// emailService.createEmail(emailAddress.getValue(), m.getSubject(), getFormattedXML(m.getBody()), m.getFileAttachments());
				}

				// emailService.createEmail(email, m.getSubject(), m.getBody());
			}
			if (logger.isInfoEnabled())
				logger.info("Sent email to: " + emailAddress);

			// }

			/*
			 * catch (EmailException e) { logger.error("EmailException: " + e); throw e;
			 * 
			 * } catch (MessagingException e) { logger.error("MessagingException (is your configuration right?): " + e); throw e; }
			 */

		}
	}

	private String getFormattedXML(final String body)
	{
		return "<p/>" + body + "<p/>";
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
		t.setBody((t.getBody() == null ? "" : t.getBody()) + "<p/> <textarea rows=\"12\" cols=\"70\">" + payload
				+ "</textarea><p/>");
		return t;
	}

}
