package com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.TcTotalFailure;

public class TcTotalFailureNotificationVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(TcTotalFailure.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((TcTotalFailure) obj);
	}

	EmailVariables getVariableReplacer(TcTotalFailure obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("MasterID", obj.getPackageID());
		replacer.setVariable("TitleField", "Mediasmiths placeholder tcTotalFailure-TitleField");
		replacer.setVariable("XXX", "Mediasmiths placeholder tcTotalFailure-Location");
		replacer.setVariable("insertAssociatedDetails", "Mediasmiths placeholder tcTotalFailure-AssociatedDetails");

		return replacer;
	}

}
