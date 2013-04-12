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

import com.google.inject.Inject;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;

public class ThymeleafEmailSender
{
	public static final Logger log = Logger.getLogger(ThymeleafEmailSender.class);


	@Inject
	private ThymeleafTemplater templater;

	public void sendThymeleafEmail(String to, String subject, String body)
	{
		log.info("Sending sendThymeleafEmail email...");
		
		log.info("Sending message, body is "+body);

		String plainEmailText = "Not successfully generated";

		
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
		
			log.info("Sending message...");

			message.setSubject(subject);

			// message.setText(body);
			Multipart multipart = new MimeMultipart("alternative");

			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(plainEmailText);
			multipart.addBodyPart(textPart);

			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(body, "text/html");
			multipart.addBodyPart(htmlPart);

			message.setContent(multipart);

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