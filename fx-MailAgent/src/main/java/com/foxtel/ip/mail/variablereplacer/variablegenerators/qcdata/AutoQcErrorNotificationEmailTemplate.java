package com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.AutoQCErrorNotification;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class AutoQcErrorNotificationEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(AutoQCErrorNotification.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		getTemplate(template, (AutoQCErrorNotification) obj);
	}

	void getTemplate(MailTemplate template, AutoQCErrorNotification obj)
	{

		template.setSubject(template.getSubject() + obj.getAssetId() + " " + obj.getJobName());

	}

}
