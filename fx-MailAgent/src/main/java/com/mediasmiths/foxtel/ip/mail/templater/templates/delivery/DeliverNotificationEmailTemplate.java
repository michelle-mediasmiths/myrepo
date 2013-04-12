package com.mediasmiths.foxtel.ip.mail.templater.templates.delivery;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.DeliveryDetails;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

public class DeliverNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	
	@Override
	public boolean handles(final Object obj)
	{
		return obj != null && obj instanceof DeliveryDetails;
	}

	@Override
	public MailTemplate customiseTemplate(final Object obj, final String comment, String templateName, ThymeleafTemplater templater)
	{
		DeliveryDetails d = (DeliveryDetails)obj;

		MailTemplate m = new MailTemplate();

		m.setEmailaddresses(getEmailaddresses());
		m.setSubject(String.format(getSubject(), d.getAssetID(), d.getTitle()));

		TemplateCall call = templater.template(templateName);
		m.setBody(call.process());
		
		return m;
	}
}
