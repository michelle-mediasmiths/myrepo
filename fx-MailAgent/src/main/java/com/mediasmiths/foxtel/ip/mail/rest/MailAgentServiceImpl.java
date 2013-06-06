package com.mediasmiths.foxtel.ip.mail.rest;

import com.foxtel.ip.mailclient.MailAgentService;
import com.foxtel.ip.mailclient.ServiceCallerEntity;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.email.Emailaddress;
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
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

	private static final transient Logger logger = Logger.getLogger(MailAgentServiceImpl.class);

	private final JAXBSerialiser JAXB;

	@Inject
	protected EmailSenderService emailService;

	@Inject
	private Provider<EventMailConfiguration> emailConfigProvider;

	@Inject
	@Named("mail.agent.send.mails")
	Boolean sendMails;

	public MailAgentServiceImpl() throws Exception
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

		if (logger.isTraceEnabled())
			logger.trace("SendMail called, will search configuration to see if any mails match this event name and namespace");

		EventMailConfiguration emailConfig = emailConfigProvider.get();
		EmailTemplateGenerator mailTemplate = emailConfig.getTemplate(caller.eventName, caller.namespace);

		if (mailTemplate != null)
		{
			logger.info("Email event registered for: " + caller.eventName);


			caller.setPayload(objXMLFor(caller));

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
				logger.info(String.format("No email configured for name: \"%s\" and namespace \"%s\"",caller.eventName,caller.namespace));
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

	private MailTemplate getMailTemplate(final ServiceCallerEntity caller, final EmailTemplateGenerator mailTemplate)
	{
		try
		{
			Object payloadObj = JAXB.deserialise(caller.payload);

			if (mailTemplate.handles(payloadObj))
			{
			    return mailTemplate.customiseTemplate(payloadObj, caller.comment);
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
			logger.info(caller.payload);
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
		t.setBody((t.getBody()== null? "":t.getBody())   + "<p/> <textarea rows=\"12\" cols=\"70\">" + payload + "</textarea><p/>");
		return t;
	}


	private void sendEmailsForEvent(final MailTemplate m) throws EmailException, MessagingException
	{

		for (Emailaddress emailAddress : m.getEmailaddresses().getEmailaddress())
		{
			logger.info("Sending email to: " + emailAddress.getValue() + " with Subject: " + m.getSubject());

			if (sendMails.booleanValue())
			{
				try
				{

					emailService.createEmail(emailAddress.getValue(),
					                         m.getSubject(),
					                         getFormattedXML(m.getBody()),
					                         m.getFileAttachments(),
					                         m.getAttachments());

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
			}
			else
			{
				logger.info("email sending has been disabled, email body read" + m.getBody());
			}
		}
	}

	private String getFormattedXML(final String body)
	{
		return "<p/>" + body + "<p/>";
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
