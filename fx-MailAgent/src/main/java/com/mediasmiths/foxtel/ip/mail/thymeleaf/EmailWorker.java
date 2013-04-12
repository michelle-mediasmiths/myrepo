package com.mediasmiths.foxtel.ip.mail.thymeleaf;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;
import com.mediasmiths.std.guice.web.rest.templating.Templater;



public class EmailWorker implements Runnable
{
	public static final Logger log = Logger.getLogger(EmailWorker.class);

	private final String subject;
	private final Templater templater;
	private final String templateName;

	public EmailWorker(
			String subject,
			Templater templater,
			String templateName)
	{
		this.subject = subject;
		this.templater = templater;
		this.templateName = templateName;
	}

	@Override
	public void run()
	{
		try
		{
			TemplateCall call = templater.template(templateName);
			log.info("Sending email...");
			String plainEmailText = "";
			String htmlEmailText = "Not successfully generated";


			call.set("job", "testID");

			htmlEmailText = call.process();
			log.trace("HTML Email:\n" + htmlEmailText);

			sendEmail(subject, plainEmailText, htmlEmailText);
			log.info("Email sent.");
		}
		catch (Throwable t)
		{
			log.fatal(t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}
	
	public void sendEmail(String subject, String textBody, String htmlBody)
	{
		
		final String username = "matthew.mcparland@mediasmiths.com";
		final String password = "warmaster!123";

		
		log.info("Setting email properties...");
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		log.info("Email properties set.");

		Session session = Session.getInstance(props, new javax.mail.Authenticator()
		{
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(username, password);
			}
		});

		try
		{
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("matthew.mcparland@mediasmiths.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("matthew.mcparland@mediasmiths.com"));

			message.setSubject(subject);

			// message.setText(body);
			Multipart multipart = new MimeMultipart("alternative");

			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(textBody);
			multipart.addBodyPart(textPart);

			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(htmlBody, "text/html");
			multipart.addBodyPart(htmlPart);

			message.setContent(multipart);

			log.info("Sending message...");
			Transport.send(message);
			log.info("Successfully sent message");
		}
		catch (MessagingException e)
		{
			log.warn("Sending message failed: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}


}