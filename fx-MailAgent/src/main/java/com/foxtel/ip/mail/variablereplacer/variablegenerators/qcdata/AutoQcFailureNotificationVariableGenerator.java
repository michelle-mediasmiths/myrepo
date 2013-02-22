package com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.AutoQCFailureNotification;

public class AutoQcFailureNotificationVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(AutoQCFailureNotification.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((AutoQCFailureNotification) obj);
	}

	EmailVariables getVariableReplacer(AutoQCFailureNotification obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("MasterID", obj.getAssetId());
		replacer.setVariable("TitleField", obj.getJobName());

		return replacer;
	}

}
