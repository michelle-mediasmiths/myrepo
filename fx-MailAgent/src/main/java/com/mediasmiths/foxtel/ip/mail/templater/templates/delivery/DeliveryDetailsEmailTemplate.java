package com.mediasmiths.foxtel.ip.mail.templater.templates.delivery;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TxDelivered;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

public class DeliveryDetailsEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj instanceof TxDelivered;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment, String templateName, ThymeleafTemplater templater)
	{
         MailTemplate t = new MailTemplate();

		TxDelivered dd = (TxDelivered)obj;

		t.setSubject(String.format(getSubject(), dd.getPackageId(), dd.getTaskId()));
		t.setEmailaddresses(getEmailaddresses());

		TemplateCall call = templater.template(templateName);
		call.set("DeliveryLocation",  dd.getStage());

		t.setBody(call.process());
		
		return t;
	}



}
