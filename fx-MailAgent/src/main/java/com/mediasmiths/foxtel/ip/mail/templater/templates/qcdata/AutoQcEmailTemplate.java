package com.mediasmiths.foxtel.ip.mail.templater.templates.qcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.AutoQCResultNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class AutoQcEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof AutoQCResultNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{

		MailTemplate t = new MailTemplate();

		AutoQCResultNotification aqce = (AutoQCResultNotification) obj;

		t.setEmailaddresses(getEmailaddresses());
		t.setSubject(String.format(getSubject(), aqce.getJobId(), aqce.getAssetId(), aqce.getTitle()));
		t.setBody(String.format(getBody(),  aqce.getAssetId()));
		return t;

	}



}
