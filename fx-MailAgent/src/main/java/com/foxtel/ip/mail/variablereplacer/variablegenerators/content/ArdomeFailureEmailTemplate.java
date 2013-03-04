package com.foxtel.ip.mail.variablereplacer.variablegenerators.content;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.ArdomeJobFailure;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class ArdomeFailureEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(ArdomeJobFailure.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{
		ArdomeJobFailure ajf = (ArdomeJobFailure) obj;

		template.setSubject(template.getSubject() + ajf.getAssetID() + " " + ajf.getJobID());
	}

}
