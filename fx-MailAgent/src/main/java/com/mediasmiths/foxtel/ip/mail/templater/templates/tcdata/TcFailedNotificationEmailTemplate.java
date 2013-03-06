package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcFailureNotification;

public class TcFailedNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	public TcFailedNotificationEmailTemplate()
	{

	}

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TcFailureNotification.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{

		return getTemplate((TcFailureNotification) obj);
	}

	MailTemplate getTemplate(TcFailureNotification obj)
	{
		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject(), obj.getPackageID(), ""));
		t.setBody(getBody());
		t.setEmailaddresses(getEmailaddresses());

		return t;
	}

}
