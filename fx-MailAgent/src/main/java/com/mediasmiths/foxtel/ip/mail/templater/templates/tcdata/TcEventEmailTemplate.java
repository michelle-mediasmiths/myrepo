package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcEvent;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class TcEventEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	public TcEventEmailTemplate()
	{

	}

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof TcEvent;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();

		TcEvent tce = (TcEvent)obj;
		
		t.setSubject(String.format(getSubject(), tce.getPackageID(), tce.getTitle()));
		t.setBody(String.format(getBody()));
		t.setEmailaddresses(getEmailaddresses());

		return t;
	}

}
