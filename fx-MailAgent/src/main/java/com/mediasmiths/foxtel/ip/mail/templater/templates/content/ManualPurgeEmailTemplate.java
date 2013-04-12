package com.mediasmiths.foxtel.ip.mail.templater.templates.content;


import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.PurgeNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

public class ManualPurgeEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{
	
	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof PurgeNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment, String templateName, ThymeleafTemplater templater)
	{
		PurgeNotification ajf = (PurgeNotification) obj;
		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject(), ajf.getAssetType(), ajf.getHouseId()));
		t.setEmailaddresses(getEmailaddresses());
		
		TemplateCall call = templater.template(templateName);
		call.set("PurgeTime", ajf.getTime());

		t.setBody(call.process());
		
		return t;
	}

}

