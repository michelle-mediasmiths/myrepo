package com.mediasmiths.foxtel.ip.mail.templater.templates.qcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.QcServerFail;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class QcServerFailEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof QcServerFail;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{

		MailTemplate t = new MailTemplate();

		QcServerFail qcsf = (QcServerFail) obj;
		t.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(), qcsf.getChannelGroup()));
		
		t.setSubject(String.format(getSubject(), qcsf.getMaterialID(), qcsf.getTitle()));
		t.setBody(String.format(getBody()));

		return t;

	}



}
