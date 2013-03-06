package com.mediasmiths.foxtel.ip.mail.templater.templates.material;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import org.apache.log4j.Logger;

public class MaterialNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	private static final transient Logger logger = Logger.getLogger(Material.class);

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(Material.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();

		Material m = (Material)obj;

		t.setBody(getBody());
		t.setEmailaddresses(getEmailaddresses());
		t.setSubject(String.format(getSubject(), m.getTitle()));

		return t;
	}


}
