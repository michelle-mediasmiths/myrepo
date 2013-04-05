package com.mediasmiths.foxtel.ip.mail.templater.templates.channelconditions;

import java.util.List;

import com.mediasmiths.foxtel.ip.common.email.Emailaddresses;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.ChannelConditionsFound;

import com.mediasmiths.foxtel.ip.mail.templater.EmailListGroupFilter;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class ChannelConditionEmailTemplater extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof ChannelConditionsFound;
	}
		

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();

		ChannelConditionsFound ccf = (ChannelConditionsFound) obj;


		Emailaddresses emailaddresses = getEmailaddresses();
		List<String> channelGroups = ccf.getChannelGroup();
		t.setEmailaddresses(EmailListGroupFilter.filterByGroups(channelGroups, emailaddresses));
		
		t.setSubject(String.format(getSubject(), ccf.getMaterialID()));
		t.setBody(String.format(getBody()));

		return t;
	}

}
