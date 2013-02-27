package com.foxtel.ip.mail.variablereplacer.variablegenerators.system;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingMarker;

public class ComplianceLoggingMarkerVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(ComplianceLoggingMarker.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((ComplianceLoggingMarker) obj);
	}

	EmailVariables getVariableReplacer(ComplianceLoggingMarker obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("MasterID", obj.getMasterID());
		replacer.setVariable("TitleField", obj.getTitleField());
		replacer.setVariable("InsertLoggerDetails", obj.getLoggerdetails());

		return replacer;
	}

}
