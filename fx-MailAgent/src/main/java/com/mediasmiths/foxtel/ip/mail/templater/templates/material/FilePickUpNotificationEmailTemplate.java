package com.mediasmiths.foxtel.ip.mail.templater.templates.material;

import java.util.List;

import com.mediasmiths.foxtel.ip.common.email.Emailaddresses;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.FilePickupDetails;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListGroupFilter;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class FilePickUpNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{


	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof FilePickupDetails;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();

		FilePickupDetails m = (FilePickupDetails)obj;

		Emailaddresses emailaddresses = getEmailaddresses();
		List<String> channelGroups = m.getChannelGroup();
		t.setEmailaddresses(EmailListGroupFilter.filterByGroups(channelGroups, emailaddresses));
		
		t.setBody(getBody());
		t.setSubject(String.format(getSubject(), m.getFilename()));
		t.setBody(String.format(getBody(), m.getFilename()));
		return t;
	}


}
