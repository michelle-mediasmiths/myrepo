package com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcTotalFailure;

public class TcTotalFailureNotificationEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TcTotalFailure.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		getVariableReplacer(template, (TcTotalFailure) obj);
	}

	void getVariableReplacer(MailTemplate template, TcTotalFailure obj)
	{

		template.setSubject(template.getSubject() + obj.getPackageID());
		//replacer.setVariable("TitleField", "Mediasmiths placeholder tcTotalFailure-TitleField");
		//replacer.setVariable("XXX", "Mediasmiths placeholder tcTotalFailure-Location");
		//replacer.setVariable("insertAssociatedDetails", "Mediasmiths placeholder tcTotalFailure-AssociatedDetails");

	}

}
