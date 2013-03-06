package com.mediasmiths.foxtel.ip.mail.templater.templates.content;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.ArdomeJobFailure;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class ArdomeFailureEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(ArdomeJobFailure.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		ArdomeJobFailure ajf = (ArdomeJobFailure) obj;
		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject(), ajf.getAssetID(), ajf.getJobID()));
		t.setBody(getBody());
		t.setEmailaddresses(getEmailaddresses());
		return t;
	}

}
