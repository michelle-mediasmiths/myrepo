package com.foxtel.ip.mail.variablereplacer.variablegenerators.system;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.CommFailure;

public class CommErrorNotificationVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(CommFailure.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((CommFailure) obj);
	}

	EmailVariables getVariableReplacer(CommFailure obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("XXX", "Mediasmiths placeholder Commerror");
		// replacer.setVariable("MasterID", obj.getPackageID());

		return replacer;
	}

}
