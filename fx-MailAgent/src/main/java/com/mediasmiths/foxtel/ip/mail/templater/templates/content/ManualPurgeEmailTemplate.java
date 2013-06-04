package com.mediasmiths.foxtel.ip.mail.templater.templates.content;


import com.mediasmiths.foxtel.ip.common.email.Emailaddresses;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.PurgeNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListGroupFilter;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import java.util.List;

public class ManualPurgeEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof PurgeNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		PurgeNotification ajf = (PurgeNotification) obj;
		MailTemplate t = new MailTemplate();


		Emailaddresses emailaddresses = getEmailaddresses();
		List<String> channelGroups = ajf.getChannelGroup();

		t.setEmailaddresses(EmailListGroupFilter.filterByGroups(channelGroups, emailaddresses));

		t.setSubject(String.format(getSubject(), ajf.getHouseId(), ajf.getTitle()));
		t.setBody(String.format(getBody(), ajf.getTime()));

		return t;
	}

}

