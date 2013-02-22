package com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.AutoQCPassNotification;

public class AutoQcPassedNotificationVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(AutoQCPassNotification.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((AutoQCPassNotification) obj);
	}

	EmailVariables getVariableReplacer(AutoQCPassNotification obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("MasterID", obj.getAssetId());
		replacer.setVariable("TitleField", obj.getJobName());

		return replacer;
	}

}
