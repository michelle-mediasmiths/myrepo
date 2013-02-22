package com.foxtel.ip.mail.variablereplacer.variablegenerators.material;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;

public class ProgrammeMaterialTypeNotificationVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(ProgrammeMaterialType.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((ProgrammeMaterialType) obj);
	}

	EmailVariables getVariableReplacer(ProgrammeMaterialType obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("MasterID", obj.getMaterialID());
		// replacer.setVariable("MasterID", obj.getAdditionalProgrammeDetail().toString());
		// replacer.setVariable("MasterID", obj.getRequiredFormat());

		return replacer;
	}

}
