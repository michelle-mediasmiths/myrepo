package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.wf.adapter.model.TCPassedNotification;

public class TcPassedNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TCPassedNotification.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate m = new MailTemplate();

		//TCPassedNotification tcPassed = (TCPassedNotification)obj;

		m.setBody((String)obj);
		m.setEmailaddresses(getEmailaddresses());
		m.setSubject(getSubject());
/*
		if (tcPassed.getAssetID()!=null)
			m.setSubject(String.format(getSubject(), tcPassed.getAssetID(), tcPassed.getTitle()));
		else
			m.setSubject(getSubject() + "Could not find packageID");
*/
		return m;

	}


}
