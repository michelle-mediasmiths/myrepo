package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcPassedNotification;

public class TcPassedNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TcPassedNotification.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate m = new MailTemplate();

		TcPassedNotification tcPassed = (TcPassedNotification)obj;

		m.setBody(getBody());
		m.setEmailaddresses(getEmailaddresses());

		if (tcPassed.getPackageID()!=null)
			m.setSubject(getSubject() + tcPassed.getPackageID());
		else
			m.setSubject(getSubject() + "Could not find packageID");

		return m;

	}


}
