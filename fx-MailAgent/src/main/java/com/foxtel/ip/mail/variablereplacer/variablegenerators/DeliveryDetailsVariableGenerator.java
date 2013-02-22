package com.foxtel.ip.mail.variablereplacer.variablegenerators;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.DeliveryDetails;

public class DeliveryDetailsVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(DeliveryDetails.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((DeliveryDetails) obj);
	}

	EmailVariables getVariableReplacer(DeliveryDetails obj)
	{

		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("insert associated details", "Master ID: "+obj.getMasterId());
		replacer.setVariable("XXX", obj.getFileLocation());

		return replacer;
	}

}
