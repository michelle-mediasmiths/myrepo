package com.mediasmiths.foxtel.ip.mail.templater.templates.delivery;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.DeliveryDetails;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class DeliveryDetailsEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj instanceof DeliveryDetails;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
         MailTemplate t = new MailTemplate();

		DeliveryDetails dd = (DeliveryDetails)obj;

		t.setSubject(String.format(getSubject(), dd.getPackageID(), dd.getJobName()));
		t.setEmailaddresses(getEmailaddresses());
		t.setBody(String.format(getBody(), dd.getStage()));

		return t;
	}



}
