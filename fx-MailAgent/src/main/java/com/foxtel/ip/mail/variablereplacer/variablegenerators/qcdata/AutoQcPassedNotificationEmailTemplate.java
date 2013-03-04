package com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.AutoQCPassNotification;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class AutoQcPassedNotificationEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(AutoQCPassNotification.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		getVariableReplacer(template, (AutoQCPassNotification) obj);
	}

	void getVariableReplacer(MailTemplate template, AutoQCPassNotification obj)
	{

		template.setSubject(template.getSubject() +  obj.getAssetId() + obj.getJobName());

	}

}
