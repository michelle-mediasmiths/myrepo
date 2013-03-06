package com.mediasmiths.foxtel.ip.mail.templater.templates.qcdata;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.AutoQCFailureNotification;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class AutoQcFailureNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(AutoQCFailureNotification.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
        MailTemplate t = new MailTemplate();
		AutoQCFailureNotification aQC = (AutoQCFailureNotification) obj;

		t.setBody(String.format(getBody(), aQC.getJobName(), aQC.getAssetId()));
		t.setEmailaddresses(getEmailaddresses());
		t.setSubject(String.format(getSubject(), aQC.getAssetId()));
		return t;

	}

}
