package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.wf.adapter.model.TCFailureNotification;



public class TcFailedNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	public TcFailedNotificationEmailTemplate()
	{

	}

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TCFailureNotification.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{

		return getTemplate(/*(TCFailureNotification)*/ obj);
	}

	MailTemplate getTemplate(Object obj)
	{
		MailTemplate t = new MailTemplate();

		t.setSubject(getSubject());

		//t.setSubject(String.format(getSubject(), obj.getAssetID(), obj.getTitle()));
		t.setBody((String)obj);
		t.setEmailaddresses(getEmailaddresses());

		return t;
	}

}
