package com.mediasmiths.foxtel.ip.mail.rest;

import com.mediasmiths.foxtel.ip.common.events.EventAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import java.util.List;

public interface EmailSenderService
{

	public MultiPartEmail createEmail(String to, String subject, String body, List<String> attachmentFilePaths, List<EventAttachment> list) throws EmailException;

}
