package com.mediasmiths.foxtel.ip.mail.templater.templates.content;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.ArdomeImportFailure;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class ArdomeFailureEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof ArdomeImportFailure;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		ArdomeImportFailure aif = (ArdomeImportFailure) obj;
		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject()));
		t.setBody(getBody(), aif.getFilename(), aif.getJobID());
		t.setEmailaddresses(getEmailaddresses());
		return t;
	}

}
