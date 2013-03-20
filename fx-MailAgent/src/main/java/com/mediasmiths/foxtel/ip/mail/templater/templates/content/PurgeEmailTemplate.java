package com.mediasmiths.foxtel.ip.mail.templater.templates.content;


import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.PurgeNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class PurgeEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof PurgeNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		PurgeNotification ajf = (PurgeNotification) obj;
		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject(), ajf.getTitleID()));
		t.setBody(getBody() + " at " + ajf.getTime().toString());
		t.setEmailaddresses(getEmailaddresses());
		return t;
	}

}

