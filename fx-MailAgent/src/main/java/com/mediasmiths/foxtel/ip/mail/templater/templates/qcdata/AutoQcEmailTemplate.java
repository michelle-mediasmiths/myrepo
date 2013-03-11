package com.mediasmiths.foxtel.ip.mail.templater.templates.qcdata;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.AutoQCErrorNotification;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class AutoQcEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(AutoQCErrorNotification.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{

		MailTemplate t = new MailTemplate();

		AutoQCErrorNotification aqce = (AutoQCErrorNotification) obj;

		t.setEmailaddresses(getEmailaddresses());
		t.setSubject(String.format(getSubject(), aqce.getJobId(), aqce.getAssetId()));
		t.setBody(getBody());
		return t;

	}

	void getTemplate(MailTemplate template, AutoQCErrorNotification obj)
	{

		template.setSubject(template.getSubject() + obj.getAssetId() + " " + obj.getJobId());

	}

}
