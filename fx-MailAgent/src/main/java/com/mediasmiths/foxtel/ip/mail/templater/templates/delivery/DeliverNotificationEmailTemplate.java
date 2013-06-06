package com.mediasmiths.foxtel.ip.mail.templater.templates.delivery;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.DeliveryDetails;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class DeliverNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{
	@Override
	public boolean handles(final Object obj)
	{
		return obj != null && obj instanceof DeliveryDetails;
	}

	@Override
	public MailTemplate customiseTemplate(final Object obj, final String comment)
	{
		DeliveryDetails d = (DeliveryDetails)obj;

		MailTemplate m = new MailTemplate();

		m.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(), d.getChannelGroup()));
		m.setBody(getBody());
		m.setSubject(String.format(getSubject(), d.getAssetID(), d.getTitle()));

		return m;
	}
}
