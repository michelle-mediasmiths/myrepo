package com.mediasmiths.foxtel.ip.mail.templater.templates.system;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.ErrorReport;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class BMSErrorEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj instanceof ErrorReport;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();
		ErrorReport cf = (ErrorReport)obj;

		t.setBody(getBody());
		t.setEmailaddresses(getEmailaddresses());
		t.setSubject(String.format(getSubject(), cf.getStatus(), cf.getBmsOp(), cf.getMediaId(), cf.getTitle()));

		return t;
	}


}
