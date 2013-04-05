package com.mediasmiths.foxtel.ip.mail.templater.templates.qcdata;

import java.util.List;

import org.apache.commons.lang.StringUtils;


import com.mediasmiths.foxtel.ip.common.email.Emailaddress;
import com.mediasmiths.foxtel.ip.common.email.Emailaddresses;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.AutoQCResultNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListGroupFilter;
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

		Emailaddresses emailaddresses = getEmailaddresses();
		List<String> channelGroups = aqce.getChannelGroup();
		t.setEmailaddresses(EmailListGroupFilter.filterByGroups(channelGroups, emailaddresses));
		
		t.setSubject(String.format(getSubject(), aqce.getAssetId(), aqce.getTitle()));
		t.setBody(String.format(getBody(), aqce.getTitle(), aqce.getAssetId()));

		return t;

	}



}
