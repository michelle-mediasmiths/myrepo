package com.mediasmiths.foxtel.ip.mail.templater;


import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public interface EmailTemplateGenerator
{
	public boolean handles(Object obj);

	public MailTemplate customiseTemplate(Object obj, String comment, String templateName);

}
