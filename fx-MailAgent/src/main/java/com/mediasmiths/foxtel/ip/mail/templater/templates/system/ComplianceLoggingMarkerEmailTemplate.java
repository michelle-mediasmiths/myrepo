package com.mediasmiths.foxtel.ip.mail.templater.templates.system;

import com.mediasmiths.foxtel.ip.mail.templater.EmailListTransform;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingMarker;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

import org.apache.log4j.Logger;

public class ComplianceLoggingMarkerEmailTemplate  extends MailTemplate implements EmailTemplateGenerator
{

	private final static Logger log = Logger.getLogger(ComplianceLoggingMarkerEmailTemplate.class);

	@Override
	public boolean handles(Object obj)
	{

		return obj instanceof ComplianceLoggingMarker;
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
        MailTemplate t = new MailTemplate();
		ComplianceLoggingMarker clm = (ComplianceLoggingMarker)obj;

		t.setEmailaddresses(EmailListTransform.buildRecipientsList(getEmailaddresses(),clm.getChannelGroup(),clm.getEmailaddresses()));

		t.setSubject(String.format(getSubject(), clm.getMasterID(), clm.getTitleField()));

		log.debug("Logger details before:"+clm.getLoggerdetails());

		String converted =  convertEndOfLineForEmail(clm.getLoggerdetails());

		log.debug("Converted details:"+converted);

		t.setBody(String.format(getBody(),converted));

		return t;

	}

	/**
	 * End of line is currently set up in MayamMaterialController to be a '\n'; for emails, this needs to be translated to
	 * <br/> to output the new lines properly.
	 * @param markerDetails
	 * @return
	 */
	private String convertEndOfLineForEmail(final String markerDetails)
	{
		return markerDetails.replace("(\n)", "<br/>");
	}
}
