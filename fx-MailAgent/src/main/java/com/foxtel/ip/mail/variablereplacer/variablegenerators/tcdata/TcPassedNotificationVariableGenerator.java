package com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.TcPassedNotification;

public class TcPassedNotificationVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TcPassedNotification.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((TcPassedNotification) obj);
	}

	EmailVariables getVariableReplacer(TcPassedNotification obj)
	{

		EmailVariables replacer = new EmailVariables();
		if (obj.getPackageID()!=null)
			replacer.setVariable("MasterID", obj.getPackageID());
		else
			replacer.setVariable("MasterID", "Could not find packageID");
		replacer.setVariable("TitleField", "Mediasmiths placeholder tcPassed-TitleField");
		replacer.setVariable("XXX", "Mediasmiths placeholder tcPassed-Location");
		replacer.setVariable("insertAssociatedDetails", "Mediasmiths placeholder tcPassed-AssociatedDetails");

		return replacer;
	}

}
