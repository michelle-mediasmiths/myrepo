package com.mediasmiths.foxtel.ip.mail.templater.templates.preview;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.PreviewFailed;

public class PreviewEventEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj instanceof PreviewFailed;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{

		MailTemplate t = new MailTemplate();

		PreviewFailed pe = (PreviewFailed)obj;

		t.setEmailaddresses(getEmailaddresses());
		
		t.setSubject(String.format(getSubject(), pe.getAssetId(), pe.getTitle()));
		
		t.setBody(getBody());

		return t;

	}

}
