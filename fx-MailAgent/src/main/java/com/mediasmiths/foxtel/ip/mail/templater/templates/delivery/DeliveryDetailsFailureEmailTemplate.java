package com.mediasmiths.foxtel.ip.mail.templater.templates.delivery;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.DeliveryDetails;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class DeliveryDetailsFailureEmailTemplate extends MailTemplate implements EmailTemplateGenerator
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

		t.setSubject(String.format(getSubject(), dd.getMaterialID(), dd.getTitle()));
		t.setEmailaddresses(getEmailaddresses());
		t.setBody(String.format(getBody()));

		return t;
	}



}
