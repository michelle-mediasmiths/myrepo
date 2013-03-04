package com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcPassedNotification;

public class TcPassedNotificationEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TcPassedNotification.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{
		TcPassedNotification tcPassed = (TcPassedNotification)obj;

		if (tcPassed.getPackageID()!=null)
			template.setSubject(template.getSubject() + tcPassed.getPackageID());
		else
			template.setSubject(template.getSubject() + "Could not find packageID");

	}


}
