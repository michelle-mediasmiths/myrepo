package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;



public class TcNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{
	@Inject
	private ThymeleafTemplater templater;

	public TcNotificationEmailTemplate()
	{

	}

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof TcNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment, String templateName)
	{

		TcNotification tcn = (TcNotification)obj;

		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject(), tcn.getAssetID(), tcn.getTitle()));
		t.setEmailaddresses(getEmailaddresses());

		TemplateCall call = templater.template(templateName);
		call.set("DeliveryLocation", tcn.getDeliveryLocation());

		t.setBody(call.process());
		return t;
	}

}
