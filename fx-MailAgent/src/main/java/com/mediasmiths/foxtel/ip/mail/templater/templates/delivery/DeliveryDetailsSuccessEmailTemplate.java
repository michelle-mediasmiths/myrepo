package com.mediasmiths.foxtel.ip.mail.templater.templates.delivery;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.ExtendedPublishingDetails;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class DeliveryDetailsSuccessEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj instanceof ExtendedPublishingDetails;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
         MailTemplate t = new MailTemplate();

         ExtendedPublishingDetails dd = (ExtendedPublishingDetails)obj;

		t.setSubject(String.format(getSubject(), dd.getMaterialID(), dd.getTitle()));
		t.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(), dd.getChannelGroup()));

		t.setBody(String.format(getBody(), dd.getFileLocation()));

		return t;
	}



}
