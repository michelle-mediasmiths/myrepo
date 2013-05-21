package com.mediasmiths.foxtel.ip.mail.rest;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import com.mediasmiths.foxtel.ip.common.events.EventAttachment;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import java.util.List;

public interface EmailSenderService
{

	public MultiPartEmail createEmail(String to, String subject, String body, List<String> attachmentFilePaths, List<EventAttachment> list) throws EmailException;

	public EmailAttachment createAttachment(String path);
//
//	void sendEmailwithAttachment(MultiPartEmail email, EmailAttachment attachment) throws EmailException;
//
//	void createMimeEmail(String to, String subject, String body, List<String> fileAttachments, List<EventAttachment> eventAttachments) throws EmailException, AddressException, MessagingException;

}
