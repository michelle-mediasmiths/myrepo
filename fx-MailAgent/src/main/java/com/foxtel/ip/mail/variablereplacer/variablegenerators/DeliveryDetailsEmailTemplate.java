package com.foxtel.ip.mail.variablereplacer.variablegenerators;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.DeliveryDetails;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class DeliveryDetailsEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(DeliveryDetails.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		getVariableReplacer(template, (DeliveryDetails) obj);
	}

	void getVariableReplacer(MailTemplate template, DeliveryDetails obj)
	{

		template.setSubject(template.getSubject() + obj.getMasterId());

		template.setBody(String.format(template.getBody(), obj.getFileLocation()));

	}

}
