package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcTotalFailure;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;



public class TcTotalFailureNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TcTotalFailure.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();
		TcTotalFailure tctf = (TcTotalFailure)obj;

		t.setBody(getBody());
		t.setEmailaddresses(getEmailaddresses());
		t.setSubject(String.format(getSubject(), tctf.getAssetID(), tctf.getTitle()));

		return t;
	}


}
