package com.mediasmiths.foxtel.ip.mail.templater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mediasmiths.foxtel.ip.common.email.Emailaddress;

public class EmailListTransform
{
	/**
	 * Takes a list of Strings and returns a corresponding List<Emailaddress> 
	 * @param emails
	 * @return
	 */
	public static List<Emailaddress> toEmailAddressList(List<String> emails)
	{

		if (emails == null)
		{
			return Collections.<Emailaddress> emptyList();
		}
		else
		{
			List<Emailaddress> ret = new ArrayList<>(emails.size());

			for (String email : emails)
			{
				Emailaddress e = new Emailaddress();
				e.setValue(email);
				ret.add(e);
			}

			return ret;
		}
	}
}
