package com.mediasmiths.foxtel.ip.mail.templater.templates.preview;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.PreviewFailed;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

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

		t.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(), pe.getChannelGroup()));
		t.setSubject(String.format(getSubject(), pe.getAssetId(), pe.getTitle()));
		t.setBody(String.format(getBody(), pe.getPreviewNotes()));

		return t;

	}

}
