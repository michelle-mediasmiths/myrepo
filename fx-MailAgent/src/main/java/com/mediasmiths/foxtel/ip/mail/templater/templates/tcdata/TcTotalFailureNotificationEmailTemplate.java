package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.wf.adapter.model.TCTotalFailure;



public class TcTotalFailureNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TCTotalFailure.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();
		// TCTotalFailure tctf = (TCTotalFailure)obj;

		t.setBody((String)obj);
		t.setEmailaddresses(getEmailaddresses());
		t.getSubject();

		return t;
	}


}
