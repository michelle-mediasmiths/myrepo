package com.mediasmiths.foxtel.ip.mail.templater.templates.qcdata;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.AutoQCResultNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class AutoQcEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof AutoQCResultNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{

		MailTemplate t = new MailTemplate();

		AutoQCResultNotification aqce = (AutoQCResultNotification) obj;
		t.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(), aqce.getChannelGroup()));
		
		t.setSubject(String.format(getSubject(), aqce.getAssetId(), aqce.getTitle()));

		if (aqce.getQcReportFilePath() != null && aqce.getQcReportFilePath().size() != 0)
		{
			for (String path: aqce.getQcReportFilePath())
			{
				t.getFileAttachments().add(path);
			}
		}

		t.setBody(String.format(getBody(), aqce.getTitle(), aqce.getAssetId()));

		return t;

	}



}
