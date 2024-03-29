package com.mediasmiths.foxtel.ip.mail.templater.templates.content;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.ArdomeImportFailure;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class ArdomeImportFailureEmailTemplate extends MailTemplate implements EmailTemplateGenerator
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
		t.setBody(String.format(getBody(), aif.getFilename(), aif.getJobID()));

		t.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(), aif.getChannelGroup()));
		return t;
	}

}
