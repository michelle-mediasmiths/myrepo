package com.mediasmiths.foxtel.ip.mail.templater.templates.material;

import java.util.List;

import com.mediasmiths.foxtel.ip.common.email.Emailaddresses;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.MediaPickupNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailListGroupFilter;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

public class MediaPickUpNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{	
	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof MediaPickupNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment, String templateName, ThymeleafTemplater templater)
	{
		MailTemplate t = new MailTemplate();

		MediaPickupNotification m = (MediaPickupNotification)obj;

		Emailaddresses emailaddresses = getEmailaddresses();
		List<String> channelGroups = m.getChannelGroup();
		t.setEmailaddresses(EmailListGroupFilter.filterByGroups(channelGroups, emailaddresses));
		
		t.setBody(getBody());
		t.setSubject(String.format(getSubject(), m.getFilelocation()));
		t.setBody(String.format(getBody(), m.getFilelocation()));
		
		TemplateCall call = templater.template(templateName);
		call.set("Placeholder", m.getFilelocation());
		call.set("QuarantinedLocation", m.getFilelocation());
		call.set("Material", m.getFilelocation());

		t.setBody(call.process());
		return t;
	}


}
