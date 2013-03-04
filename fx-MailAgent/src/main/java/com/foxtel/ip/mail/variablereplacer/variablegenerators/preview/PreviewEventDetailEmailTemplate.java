package com.foxtel.ip.mail.variablereplacer.variablegenerators.preview;

import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.PreviewEventDetail;

public class PreviewEventDetailEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(PreviewEventDetail.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{

		getVariableReplacer(template, (PreviewEventDetail) obj);
	}

	void getVariableReplacer(MailTemplate template, PreviewEventDetail obj)
	{


	}

}
