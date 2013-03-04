package com.foxtel.ip.mail.variablereplacer.variablegenerators.material;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class MarketingMaterialTypeNotificationEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(MarketingMaterialType.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		customise(template, (MarketingMaterialType) obj);
	}

	void customise(MailTemplate template, MarketingMaterialType obj)
	{

		// replacer.setVariable("MasterID", "MarkertMaterial Type Does not have Id yet");

	}

}
