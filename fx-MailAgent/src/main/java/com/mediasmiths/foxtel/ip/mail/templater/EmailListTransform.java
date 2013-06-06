package com.mediasmiths.foxtel.ip.mail.templater;

import com.mediasmiths.foxtel.ip.common.email.Emailaddress;
import com.mediasmiths.foxtel.ip.common.events.Emailaddresses;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmailListTransform
{
	private final static Logger log = Logger.getLogger(EmailListTransform.class);


	/**
	 * Takes a list of Strings and returns a corresponding List<Emailaddress>
	 *
	 * @param emails
	 *
	 * @return
	 */
	private static List<Emailaddress> toEmailAddressList(Emailaddresses emails)
	{

		if (emails == null || emails.getEmailaddress() == null)
		{
			return Collections.<Emailaddress>emptyList();
		}
		else
		{
			List<Emailaddress> ret = new ArrayList<>(emails.getEmailaddress().size());

			for (String email : emails.getEmailaddress())
			{
				Emailaddress e = new Emailaddress();
				e.setValue(email);
				ret.add(e);
			}

			return ret;
		}
	}

	/**
	 * Builds the recipients list to use for a given combination of addresses that should always get a particular type of emails
	 * (templateAddresses) and channel groups associated with the event that the email resulted from
	 *
	 * @param templateAddresses -  Addresses configured to receive this email
	 * @param channelGroups     - Any channel groups the event is associated with
	 *
	 * @return
	 */
	public static com.mediasmiths.foxtel.ip.common.email.Emailaddresses buildRecipientsList(com.mediasmiths.foxtel.ip.common.email.Emailaddresses templateAddresses,
	                                                                                        List<String> channelGroups)
	{
		return buildRecipientsList(templateAddresses, channelGroups, null);
	}

	/**
	 * Builds the recipients list to use for a given combination of addresses that should always get a particular type of emails
	 * (templateAddresses), channel groups associated with the event that the email resulted from, as well as any email addresess
	 * associated with the event itself
	 *
	 * @param templateAddresses -  Addresses configured to receive this email
	 * @param channelGroups     - Any channel groups the event is associated with
	 * @param eventAddresses    - Any addresses the event is associated with (for example the user who created a task)
	 *
	 * @return
	 */
	public static com.mediasmiths.foxtel.ip.common.email.Emailaddresses buildRecipientsList(com.mediasmiths.foxtel.ip.common.email.Emailaddresses templateAddresses,
	                                                 List<String> channelGroups,
	                                                 Emailaddresses eventAddresses)
	{

		com.mediasmiths.foxtel.ip.common.email.Emailaddresses ret = new com.mediasmiths.foxtel.ip.common.email.Emailaddresses();

		//first the address that are predefined for this email
		if (templateAddresses != null && templateAddresses.getEmailaddress().size() > 0)
		{
			//addresses that are predefined for this email by be filtered based on any channel group association the event might have
			ret.getEmailaddress().addAll(EmailListGroupFilter.filterByGroups(channelGroups, templateAddresses).getEmailaddress());
		}
		else
		{
			log.debug("No predefined addresses for this email");
		}

		//add any email addresses associated with the event
		if (eventAddresses != null && eventAddresses.getEmailaddress().size() > 0)
		{
			ret.getEmailaddress().addAll(EmailListTransform.toEmailAddressList(eventAddresses));
		}
		else
		{
			log.debug("No email addresses associated with event");
		}

		return ret;
	}
}
