package com.mediasmiths.foxtel.ip.mail.templater.templates.material;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.MediaPickupNotification;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;

public class MediaPickUpNotificationEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{


	@Override
	public boolean handles(Object obj)
	{
		return obj instanceof MediaPickupNotification;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		MailTemplate t = new MailTemplate();

		MediaPickupNotification m = (MediaPickupNotification)obj;

		t.setBody(getBody());
		t.setEmailaddresses(getEmailaddresses());
		t.setSubject(String.format(getSubject(), m.getFilelocation()));

		return t;
	}


}
