package com.foxtel.ip.mail.variablereplacer.variablegenerators.material;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class MaterialTypeNotificationEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(MaterialType.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{
		getVariableReplacer(template, (MaterialType) obj);
	}

	void getVariableReplacer(MailTemplate template, MaterialType obj)
	{



	}

}
