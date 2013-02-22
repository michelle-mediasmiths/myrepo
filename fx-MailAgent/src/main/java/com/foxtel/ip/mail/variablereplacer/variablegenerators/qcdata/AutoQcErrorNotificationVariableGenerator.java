package com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.AutoQCErrorNotification;

public class AutoQcErrorNotificationVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(AutoQCErrorNotification.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((AutoQCErrorNotification) obj);
	}

	EmailVariables getVariableReplacer(AutoQCErrorNotification obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("MasterID", obj.getAssetId());
		replacer.setVariable("TitleField", obj.getJobName());

		return replacer;
	}

}
