package com.foxtel.ip.mail.variablereplacer.variablegenerators.material;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;

public class MarketingMaterialTypeNotificationVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(MarketingMaterialType.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((MarketingMaterialType) obj);
	}

	EmailVariables getVariableReplacer(MarketingMaterialType obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("MasterID", "MarkertMaterial Type Does not have Id yet");
		// replacer.setVariable("MasterID", obj.getRequiredFormat());

		return replacer;
	}

}
