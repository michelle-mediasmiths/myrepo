package com.mediasmiths.foxtel.ip.mail.rest;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.EventAttachment;
import com.mediasmiths.foxtel.ip.mail.data.EmailProperties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;

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
	 * Creates the email attachment given the filepath
	 * 
	 * @param path
	 * @return attachment
	 */
	@Override
	public EmailAttachment createAttachment(String path)
	{

		EmailAttachment attachment = new EmailAttachment();
		attachment.setPath(path);
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription("FoxtelAttachement");
		attachment.setName("Attachment");

		return attachment;
	}

	/**
	 * 
	 * 
	 * @param email
	 */
	public void sendEmail(MultiPartEmail email) throws EmailException
	{
		if (logger.isTraceEnabled())
			logger.info("Email about to be sent: " + email.toString());

		email.send();
		if (logger.isTraceEnabled())
			logger.info("Email sent");

	}


}
