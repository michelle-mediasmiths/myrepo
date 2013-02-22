package com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.TcFailureNotification;

public class TcFailedNotificationVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TcFailureNotification.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((TcFailureNotification) obj);
	}

	EmailVariables getVariableReplacer(TcFailureNotification obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("MasterID", obj.getPackageID());
		replacer.setVariable("TitleField", "Mediasmiths placeholder tcfailed-TitleField");
		replacer.setVariable("XXX", "Mediasmiths placeholder tcfailed-Location");
		replacer.setVariable("insertAssociatedDetails", "Mediasmiths placeholder tcfailed-AssociatedDetails");

		return replacer;
	}

}
