package com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcFailureNotification;

public class TcFailedNotificationEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TcFailureNotification.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		getTemplate(template, (TcFailureNotification) obj);
	}

	void getTemplate(MailTemplate template, TcFailureNotification obj)
	{

		template.setSubject(template.getSubject() + obj.getPackageID());
		//template.setSubject("TitleField", "Mediasmiths placeholder tcfailed-TitleField");
		// template.setVariable("XXX", "Mediasmiths placeholder tcfailed-Location");
		// template.setVariable("insertAssociatedDetails", "Mediasmiths placeholder tcfailed-AssociatedDetails");

	}

}
