package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcPassedNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

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

		m.setBody((String)obj);
		m.setEmailaddresses(getEmailaddresses());
		m.setSubject(getSubject());

		if (tcPassed.getAssetID()!=null)
			m.setSubject(String.format(getSubject(), tcPassed.getAssetID(), tcPassed.getTitle()));
		else
			m.setSubject(getSubject() + "Could not find packageID");

		return m;

	}


}
