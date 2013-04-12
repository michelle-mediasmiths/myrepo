package com.mediasmiths.foxtel.ip.mail.templater;


import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;

public interface EmailTemplateGenerator
{
	public boolean handles(Object obj);

	public MailTemplate customiseTemplate(Object obj, String comment, String templateName, ThymeleafTemplater templater);

}
