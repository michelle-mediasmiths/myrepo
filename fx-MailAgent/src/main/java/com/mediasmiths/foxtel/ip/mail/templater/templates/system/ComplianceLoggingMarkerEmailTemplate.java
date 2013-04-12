package com.mediasmiths.foxtel.ip.mail.templater.templates.system;

import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingMarker;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

public class ComplianceLoggingMarkerEmailTemplate  extends MailTemplate implements EmailTemplateGenerator
{
	@Override
	public boolean handles(Object obj)
	{

		return obj instanceof ComplianceLoggingMarker;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment, String templateName, ThymeleafTemplater templater)
	{
        MailTemplate t = new MailTemplate();
		ComplianceLoggingMarker clm = (ComplianceLoggingMarker)obj;

		t.setEmailaddresses(getEmailaddresses());
		t.getEmailaddresses().getEmailaddress().addAll(EmailListTransform.toEmailAddressList(clm.getEmailaddresses().getEmailaddress()));

		t.setSubject(String.format(getSubject(), clm.getMasterID(), clm.getTitleField()));

		t.setBody(String.format(getBody(), clm.getLoggerdetails()));
		
		TemplateCall call = templater.template(templateName);
		call.set("ItemDetail", clm.getLoggerdetails());

		t.setBody(call.process());

		return t;

	}


}
