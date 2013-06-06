package com.mediasmiths.foxtel.ip.mail.templater.templates.delivery;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TxDelivered;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class DeliveryDetailsEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj instanceof TxDelivered;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
         MailTemplate t = new MailTemplate();

		TxDelivered dd = (TxDelivered)obj;

		t.setSubject(String.format(getSubject(), dd.getPackageId(), dd.getTaskId()));
		t.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(), dd.getChannelGroup()));
		t.setBody(String.format(getBody(), dd.getStage()));

		return t;
	}



}
