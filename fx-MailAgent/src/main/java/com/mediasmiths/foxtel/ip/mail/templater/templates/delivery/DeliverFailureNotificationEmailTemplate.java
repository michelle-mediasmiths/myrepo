package com.mediasmiths.foxtel.ip.mail.templater.templates.delivery;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.DeliveryDetails;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class DeliverFailureNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
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

		m.setEmailaddresses(getEmailaddresses());
		m.setBody(getBody());
		m.setSubject(String.format(getSubject(), d.getAssetID(), d.getTitle()));

		return m;
	}
}
