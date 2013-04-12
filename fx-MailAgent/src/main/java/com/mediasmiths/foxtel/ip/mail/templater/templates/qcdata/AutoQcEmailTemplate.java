package com.mediasmiths.foxtel.ip.mail.templater.templates.qcdata;

import com.mediasmiths.foxtel.ip.common.email.Emailaddresses;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.AutoQCResultNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListGroupFilter;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

import java.util.List;

public class AutoQcEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{
	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof AutoQCResultNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment, String templateName, ThymeleafTemplater templater)
	{

		MailTemplate t = new MailTemplate();

		AutoQCResultNotification aqce = (AutoQCResultNotification) obj;

		Emailaddresses emailaddresses = getEmailaddresses();
		List<String> channelGroups = aqce.getChannelGroup();
		t.setEmailaddresses(EmailListGroupFilter.filterByGroups(channelGroups, emailaddresses));
		
		t.setSubject(String.format(getSubject(), aqce.getAssetId(), aqce.getTitle()));

		if (aqce.getQcReportFilePath() != null && aqce.getQcReportFilePath().size() != 0)
		{
			for (String path: aqce.getQcReportFilePath())
			{
				t.getFileAttachments().add(path);
			}
		}

		t.setBody(String.format(getBody(), aqce.getTitle(), aqce.getAssetId()));

		TemplateCall call = templater.template(templateName);
		call.set("AssetID", aqce.getAssetId());
		call.set("Title", aqce.getTitle());
		t.setBody(call.process());
		
		return t;

	}



}
