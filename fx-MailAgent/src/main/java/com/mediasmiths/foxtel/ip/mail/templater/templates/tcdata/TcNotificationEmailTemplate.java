package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcEvent;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;


public class TcNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	public TcNotificationEmailTemplate()
	{

	}


	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof TcEvent;
	}


	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{

		return getTemplate((TcEvent) obj);
	}


	MailTemplate getTemplate(TcEvent obj)
	{
		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject(), obj.getAssetID(), obj.getTitle()));
		t.setBody(String.format(getBody(), obj.getDeliveryLocation()));

		t.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(),obj.getChannelGroup(),obj.getEmailaddresses()));

		t.getAttachments().addAll(obj.getAttachments());

		return t;
	}
}
