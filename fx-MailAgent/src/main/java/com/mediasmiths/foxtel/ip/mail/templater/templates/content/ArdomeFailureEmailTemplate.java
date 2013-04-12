package com.mediasmiths.foxtel.ip.mail.templater.templates.content;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.ArdomeJobFailure;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

public class ArdomeFailureEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{
	@Inject
	private ThymeleafTemplater templater;
	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof ArdomeJobFailure;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment, String templateName)
	{
		ArdomeJobFailure ajf = (ArdomeJobFailure) obj;
		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject(), ajf.getAssetID(), ajf.getJobID()));
		t.setEmailaddresses(getEmailaddresses());
		
		TemplateCall call = templater.template(templateName);
		t.setBody(call.process());
		return t;
	}

}
