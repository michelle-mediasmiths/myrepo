package com.mediasmiths.foxtel.ip.mail.templater.templates.system;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.CommFailure;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

public class CommErrorNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{
	@Inject
	private ThymeleafTemplater templater;

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(CommFailure.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment, String templateName)
	{
        MailTemplate t = new MailTemplate();

		t.setBody(getBody());
		t.setEmailaddresses(getEmailaddresses());
		t.setSubject(getSubject());

		
		TemplateCall call = templater.template(templateName);
		t.setBody(call.process());

		return t;
	}


}
