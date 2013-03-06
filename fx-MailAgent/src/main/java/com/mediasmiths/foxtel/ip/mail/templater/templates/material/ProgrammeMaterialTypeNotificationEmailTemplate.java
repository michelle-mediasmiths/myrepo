package com.mediasmiths.foxtel.ip.mail.templater.templates.material;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class ProgrammeMaterialTypeNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(ProgrammeMaterialType.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();
		ProgrammeMaterialType pm = (ProgrammeMaterialType)obj;
		t.setBody(getBody());
		t.setEmailaddresses(getEmailaddresses());
		t.setSubject(getSubject());

		return t;
	}


}
