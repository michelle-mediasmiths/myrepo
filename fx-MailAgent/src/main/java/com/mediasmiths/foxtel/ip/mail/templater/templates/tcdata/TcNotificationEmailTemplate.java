package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcFailureNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;



public class TcNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	public TcNotificationEmailTemplate()
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

		t.setSubject(getSubject());

		t.setSubject(String.format(getSubject(), obj.getAssetID(), obj.getTitle()));
		t.setBody(getBody());
		t.setEmailaddresses(getEmailaddresses());

		return t;
	}

}
