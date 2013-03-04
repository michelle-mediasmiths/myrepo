package com.foxtel.ip.mail.variablereplacer.variablegenerators.material;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;
import org.apache.log4j.Logger;

public class MaterialNotificationEmailTemplate implements EmailTemplateGenerator
{

	private static final transient Logger logger = Logger.getLogger(Material.class);

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(Material.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		 customise(template, (Material) obj);
	}

	void customise(MailTemplate template, Material obj)
	{
		template.setSubject(template.getSubject() + obj.getTitle().getTitleID());

	}

}
