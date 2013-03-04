package com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.AutoQCFailureNotification;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class AutoQcFailureNotificationEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(AutoQCFailureNotification.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		getVariableReplacer(template, (AutoQCFailureNotification) obj);
	}

	void getVariableReplacer(MailTemplate template, AutoQCFailureNotification obj)
	{

		template.setSubject(template.getSubject() + obj.getAssetId() + " " + obj.getJobName());
	}

}
