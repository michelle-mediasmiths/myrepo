package com.foxtel.ip.mail.variablereplacer.variablegenerators.material;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;

public class MaterialTypeNotificationVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(MaterialType.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{
		return getVariableReplacer((MaterialType) obj);
	}

	EmailVariables getVariableReplacer(MaterialType obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("MasterID", "unknown");

		return replacer;
	}

}
