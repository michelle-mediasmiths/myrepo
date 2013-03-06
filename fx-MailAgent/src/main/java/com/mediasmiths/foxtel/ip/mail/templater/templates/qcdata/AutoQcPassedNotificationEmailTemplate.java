package com.mediasmiths.foxtel.ip.mail.templater.templates.qcdata;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.AutoQCPassNotification;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class AutoQcPassedNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(AutoQCPassNotification.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{

		MailTemplate t = new MailTemplate();
		AutoQCPassNotification aqcp = (AutoQCPassNotification)obj;

		t.setBody(getBody());
		t.setSubject(getSubject());
		t.setEmailaddresses(getEmailaddresses());

		return t;
	}


}
