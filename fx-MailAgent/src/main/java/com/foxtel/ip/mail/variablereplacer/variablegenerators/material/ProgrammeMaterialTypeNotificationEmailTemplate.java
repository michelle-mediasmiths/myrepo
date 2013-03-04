package com.foxtel.ip.mail.variablereplacer.variablegenerators.material;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class ProgrammeMaterialTypeNotificationEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(ProgrammeMaterialType.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		getVariableReplacer(template, (ProgrammeMaterialType) obj);
	}

	void getVariableReplacer(MailTemplate template, ProgrammeMaterialType obj)
	{

		template.setSubject(template.getSubject() + obj.getMaterialID());

	}

}
