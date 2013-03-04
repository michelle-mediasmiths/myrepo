package com.foxtel.ip.mail.variablereplacer.variablegenerators.system;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingMarker;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class ComplianceLoggingMarkerEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(ComplianceLoggingMarker.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		getVariableReplacer(template, (ComplianceLoggingMarker) obj);
	}

	void getVariableReplacer(MailTemplate template, ComplianceLoggingMarker obj)
	{

		template.setSubject(template.getSubject() + obj.getMasterID() + obj.getTitleField());

		template.setBody(template.getBody() + obj.getLoggerdetails());

		template.setEmailaddresses(obj.getEmailaddresses());


	}

}
