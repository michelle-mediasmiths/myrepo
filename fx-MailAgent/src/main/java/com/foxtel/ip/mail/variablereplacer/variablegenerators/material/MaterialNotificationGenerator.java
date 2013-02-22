package com.foxtel.ip.mail.variablereplacer.variablegenerators.material;

import org.apache.log4j.Logger;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;

public class MaterialNotificationGenerator implements EmailVariableGenerator
{

	private static final transient Logger logger = Logger.getLogger(Material.class);

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(Material.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{
		if (logger.isTraceEnabled())
			logger.info("Material type");
		return getVariableReplacer((Material) obj);
	}

	EmailVariables getVariableReplacer(Material obj)
	{
		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("MasterID", obj.getTitle().getTitleID());

		return replacer;
	}

}
