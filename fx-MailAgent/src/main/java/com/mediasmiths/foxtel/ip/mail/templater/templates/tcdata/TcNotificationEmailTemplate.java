package com.mediasmiths.foxtel.ip.mail.templater.templates.tcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;



public class TcNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	public TcNotificationEmailTemplate()
	{

	}

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof TcNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{

		return getTemplate((TcNotification) obj);
	}

	MailTemplate getTemplate(TcNotification obj)
	{
		MailTemplate t = new MailTemplate();

		t.setSubject(String.format(getSubject(), obj.getAssetID(), obj.getTitle()));
		t.setBody(String.format(getBody(), obj.getDeliveryLocation()));
		t.setEmailaddresses(getEmailaddresses());
		t.getEmailaddresses().getEmailaddress().addAll(EmailListTransform.toEmailAddressList(obj.getEmailaddresses().getEmailaddress()));

		
		return t;
	}

}
