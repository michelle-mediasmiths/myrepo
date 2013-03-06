package com.mediasmiths.foxtel.ip.mail.rest;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

public interface EmailSenderService
{

	public MultiPartEmail createEmail(String to, String subject, String path) throws EmailException;

	public EmailAttachment createAttachment(String path);

	void sendEmailwithAttachment(MultiPartEmail email, EmailAttachment attachment) throws EmailException;

	void createMimeEmail(String to, String subject, String body) throws EmailException, AddressException, MessagingException;

}
