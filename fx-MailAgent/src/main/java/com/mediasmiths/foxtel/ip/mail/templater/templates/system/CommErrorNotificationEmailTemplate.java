package com.mediasmiths.foxtel.ip.mail.templater.templates.system;

import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.CommFailure;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class CommErrorNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(CommFailure.class);
	}


	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();
		CommFailure cf = (CommFailure) obj;

		StringBuilder detail = new StringBuilder();
		detail.append("Source : ")
		      .append(cf.getSource())
		      .append("<br/>")
		      .append("Target : ")
		      .append(cf.getTarget())
		      .append("<br/>")
		      .append("Error : ")
		      .append(cf.getFailureShortDesc())
		      .append("<br/>")
		      .append(cf.getFailureLongDescription())
		      .append("<br/>");

		t.setBody(String.format(getBody(), detail.toString()));
		t.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(), cf.getChannelGroup()));
		t.setSubject(getSubject());

		return t;
	}
}
