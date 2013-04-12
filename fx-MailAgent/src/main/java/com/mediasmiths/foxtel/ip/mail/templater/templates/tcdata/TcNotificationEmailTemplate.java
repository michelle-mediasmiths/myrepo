package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import org.apache.log4j.Logger;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

public class TcNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{
	private static final transient Logger logger = Logger.getLogger(TcNotificationEmailTemplate.class);

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof TcNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment, String templateName, ThymeleafTemplater templater)
	{

		TcNotification tcn = (TcNotification)obj;

		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject(), tcn.getAssetID(), tcn.getTitle()));
		t.setEmailaddresses(getEmailaddresses());

		logger.trace("Using template: " + templateName);
		TemplateCall call = templater.template(templateName);
		call.set("DeliveryLocation", tcn.getDeliveryLocation());

		t.setBody(call.process());
		return t;
	}

}
