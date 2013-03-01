package com.foxtel.ip.mail.rest;

import com.foxtel.ip.mail.data.EmailProperties;
import com.google.inject.Inject;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

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
	public MultiPartEmail createEmail(String to, String subject, String body) throws EmailException
	{
		if (logger.isTraceEnabled())
			logger.info("Creating mail message");

		MultiPartEmail email = new MultiPartEmail();

		email.setHostName(emailProperties.hostName);
		email.addTo(to);
		email.setFrom(emailProperties.emailAddress);
		email.setSmtpPort(emailProperties.smtpPort);

		//If sending through gmail (not on foxtel)
		//email.setAuthentication(emailProperties.emailAddress, emailProperties.password);
		//email.setTLS(true);

		
		email.setSubject(subject);
		email.setMsg(body);
		if (logger.isTraceEnabled())
			logger.info("Sending mail message to... " + to);

		sendEmail(email);
		return email;
	}

	/**
	 * Used to create email, email content may have html (e.g. bold for foxtel emails)
	 * 
	 * @param to
	 * @param subject
	 * @param body
	 * @throws EmailException
	 * @throws AddressException
	 * @throws javax.mail.MessagingException
	 */
	@Override
	public void createMimeEmail(String to, String subject, String body)
			throws EmailException,
			AddressException,
			javax.mail.MessagingException
	{
		if (logger.isTraceEnabled())
			logger.info("Creating Mime mail message");

		Properties props = System.getProperties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", emailProperties.hostName);
//		props.put("mail.smtp.user", emailProperties.emailAddress);
//		props.put("mail.smtp.password", emailProperties.password);
		props.put("mail.smtp.port", emailProperties.smtpPort);
		props.put("mail.smtp.auth", "false");

		logger.info("Creating Mime mail message to... " + to);

		Session session = Session.getDefaultInstance(props, null);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(emailProperties.emailAddress));

		InternetAddress toAddress = new InternetAddress(to);

		message.addRecipient(Message.RecipientType.TO, toAddress);

		message.setSubject(subject);
		message.setContent(body, "text/html");
		Transport.send(message);
//		Transport transport = session.getTransport("smtp");
//		transport.connect(emailProperties.hostName, emailProperties.emailAddress, emailProperties.password);
//		transport.sendMessage(message, message.getAllRecipients());
//		transport.close();

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

	@Override
	public void sendEmailwithAttachment(MultiPartEmail email, EmailAttachment attachment) throws EmailException
	{
		if (logger.isTraceEnabled())
			logger.info("Sending mail with attachment: " + email.toString());

		email.attach(attachment);
		email.send();
		if (logger.isTraceEnabled())
			logger.info("Email sent!");

	}

}
