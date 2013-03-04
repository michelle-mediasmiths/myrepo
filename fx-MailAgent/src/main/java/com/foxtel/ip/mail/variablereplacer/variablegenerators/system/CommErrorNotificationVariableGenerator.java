package com.foxtel.ip.mail.variablereplacer.variablegenerators.system;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.CommFailure;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class CommErrorNotificationVariableGenerator implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(CommFailure.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		 getVariableReplacer(template, (CommFailure) obj);
	}

	void getVariableReplacer(MailTemplate template, CommFailure obj)
	{

		// EmailVariables replacer = new EmailVariables();
		//replacer.setVariable("XXX", "Mediasmiths placeholder Commerror");
		// replacer.setVariable("MasterID", obj.getPackageID());

	}

}
