package com.mediasmiths.foxtel.ip.mail.rest;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.EventAttachment;
import com.mediasmiths.foxtel.ip.mail.data.EmailProperties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;

import java.util.List;

public class EmailSenderServiceImpl implements EmailSenderService
{

	private static final transient Logger logger = Logger.getLogger(EmailSenderServiceImpl.class);

	@Inject
	EmailProperties emailProperties;

	/**
	 * Reads data from email.properties file
	 */
	public EmailSenderServiceImpl()
	{

	}

	/**
	 * Creates the body and details of the email
	 * 
	 * @param to
	 * @param subject
	 * @param body
	 * @return email
	 */
	@Override
	public MultiPartEmail createEmail(String to, String subject, String body, List<String> attachmentFilePaths, List<EventAttachment> eventAttachments) throws EmailException
	{
		if (logger.isTraceEnabled())
			logger.info("Creating mail message");

		HtmlEmail email = new HtmlEmail();

		email.setHostName(emailProperties.hostName);
		email.addTo(to);
		email.setFrom(emailProperties.emailAddress);
		email.setSmtpPort(emailProperties.smtpPort);

		if (attachmentFilePaths != null && attachmentFilePaths.size() != 0)
		{
			for (String path: attachmentFilePaths)
			{
				try
				{
					logger.debug("Attaching file at: " + path);
					EmailAttachment a = new EmailAttachment();
					a.setName(FilenameUtils.getName(path));
					a.setPath(path);
					email.attach(a);
				}
				catch (EmailException e)
				{
					logger.error("Attachment of " + path + " failed.", e);
				}
			}
		}
		
		if (eventAttachments != null)
		{
			for (EventAttachment eventAttachment : eventAttachments)
			{
				try
				{
					String value = eventAttachment.getValue();
					byte[] decoded = Base64.decodeBase64(value.getBytes());
					ByteArrayDataSource source = new ByteArrayDataSource(decoded, eventAttachment.getMime());
					email.attach(source, eventAttachment.getFilename(), "");
				}
				catch (Exception e)
				{
					logger.error("error adding attachment");
				}
			}
		}

		//If sending through gmail (not on foxtel)
		//email.setAuthentication(emailProperties.emailAddress, emailProperties.password);
		//email.setTLS(true);

		
		email.setSubject(subject);
		email.setHtmlMsg(body);

		if (logger.isTraceEnabled())
			logger.info("Sending mail message to... " + to);

		sendEmail(email);

		return email;
	}

	/**
	 * 
	 * 
	 * @param email
	 */
	private void sendEmail(MultiPartEmail email) throws EmailException
	{
		if (logger.isDebugEnabled())
			logger.debug("Email about to be sent");

		email.send();

		if (logger.isTraceEnabled())
			logger.trace("Email sent");

	}


}
