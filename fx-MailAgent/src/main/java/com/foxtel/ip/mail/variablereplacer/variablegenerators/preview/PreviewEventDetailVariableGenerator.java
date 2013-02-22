package com.foxtel.ip.mail.variablereplacer.variablegenerators.preview;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.PreviewEventDetail;

public class PreviewEventDetailVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{

		return obj.getClass().equals(PreviewEventDetail.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{

		return getVariableReplacer((PreviewEventDetail) obj);
	}

	EmailVariables getVariableReplacer(PreviewEventDetail obj)
	{

		EmailVariables replacer = new EmailVariables();

		return replacer;
	}

}
