package com.mediasmiths.foxtel.ip.mail.templater;

import com.mediasmiths.foxtel.ip.common.email.Emailaddress;
import com.mediasmiths.foxtel.ip.common.events.Emailaddresses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmailListTransform
{
	/**
	 * Takes a list of Strings and returns a corresponding List<Emailaddress> 
	 *
	 * @param emails
	 * @return
	 */
	public static List<Emailaddress> toEmailAddressList(Emailaddresses emails)
	{

		if (emails == null || emails.getEmailaddress() == null)
		{
			return Collections.<Emailaddress> emptyList();
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
}
