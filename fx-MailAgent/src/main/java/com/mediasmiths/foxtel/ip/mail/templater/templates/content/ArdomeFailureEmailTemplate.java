package com.mediasmiths.foxtel.ip.mail.templater.templates.content;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.ArdomeJobFailure;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class ArdomeFailureEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof ArdomeJobFailure;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		ArdomeJobFailure ajf = (ArdomeJobFailure) obj;
		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject(), ajf.getAssetID(), ajf.getJobID()));
		t.setBody(getBody());
		t.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(), ajf.getChannelGroup()));
		return t;
	}

}
