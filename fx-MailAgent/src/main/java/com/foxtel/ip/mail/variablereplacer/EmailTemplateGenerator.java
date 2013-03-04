package com.foxtel.ip.mail.variablereplacer;


import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public interface EmailTemplateGenerator
{
	public boolean handles(Object obj);

	public void customiseTemplate(MailTemplate template, Object obj, String comment);

}
