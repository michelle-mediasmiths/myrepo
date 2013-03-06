package com.mediasmiths.foxtel.ip.mail.templater.templates.material;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class MarketingMaterialTypeNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(MarketingMaterialType.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
         MailTemplate t = new MailTemplate();

		MarketingMaterialType mm = (MarketingMaterialType)obj;

		t.setEmailaddresses(getEmailaddresses());
		t.setBody(getBody());
		t.setSubject(String.format(getSubject(), "Unknown"));

		return t;
	}



}
