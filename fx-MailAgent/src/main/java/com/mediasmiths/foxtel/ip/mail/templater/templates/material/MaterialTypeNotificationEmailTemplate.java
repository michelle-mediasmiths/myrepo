package com.mediasmiths.foxtel.ip.mail.templater.templates.material;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class MaterialTypeNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(MaterialType.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();

		MaterialType mt = (MaterialType)obj;

		t.setEmailaddresses(getEmailaddresses());
		t.setBody(getBody());
		t.setSubject(String.format(getSubject(), "unknown"));

		return t;
	}



}
