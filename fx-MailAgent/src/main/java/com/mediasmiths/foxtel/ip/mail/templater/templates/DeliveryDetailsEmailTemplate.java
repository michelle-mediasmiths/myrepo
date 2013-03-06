package com.mediasmiths.foxtel.ip.mail.templater.templates;

import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.DeliveryDetails;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class DeliveryDetailsEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(DeliveryDetails.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
         MailTemplate t = new MailTemplate();

		 DeliveryDetails dd = (DeliveryDetails)obj;

		t.setSubject(String.format(getSubject(), dd.getMasterId(), dd.getTitle()));
		t.setEmailaddresses(getEmailaddresses());
		t.setBody(String.format(getBody(), dd.getFileLocation()));

		return t;
	}



}
