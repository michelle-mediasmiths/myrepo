package com.mediasmiths.foxtel.ip.mail.templater.templates.system;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.CommFailure;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class CommErrorNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(CommFailure.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
        MailTemplate t = new MailTemplate();
		CommFailure cf = (CommFailure)obj;

		t.setBody(getBody());
		t.setEmailaddresses(getEmailaddresses());
		t.setSubject(getSubject());

		return t;
	}


}
