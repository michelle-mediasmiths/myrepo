package com.mediasmiths.foxtel.ip.mail.templater;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mediasmiths.foxtel.ip.common.email.Emailaddress;
import com.mediasmiths.foxtel.ip.common.email.Emailaddresses;

public class EmailListGroupFilter
{
	
	private final static Logger log = Logger.getLogger(EmailListGroupFilter.class);
	
	/**
	 * Filters a target email address collection against a channel groups list
	 * 
	 * returned object will have email addreses that are not associated with any groups as well as the email addresses associated with the supplied channelGroups list
	 * 
	 * @param channelGroups
	 * @param original
	 * @return
	 */
	public static Emailaddresses filterByGroups(List<String> channelGroups, Emailaddresses original){
				
		if (channelGroups.isEmpty())
		{
			return original; //if no channel groups then no filtering required
		}
		else
		{
			log.debug("event channel groups: "+ StringUtils.join(channelGroups, ','));
			
			List<Emailaddress> emails = original.getEmailaddress();
			Emailaddresses sendto = new Emailaddresses();

			for (Emailaddress email : emails)
			{
				log.debug(String.format("email {%s} group {%s}", email.getValue(),email.getChannelGroup()));
				
				if (StringUtils.isEmpty(email.getChannelGroup()))
				{ // if email address is not associated with a group then always use it
					sendto.getEmailaddress().add(email);
				}
				else if (channelGroups.contains(email.getChannelGroup()))
				{
					log.trace("adding "+email.getValue());
					sendto.getEmailaddress().add(email); // if email is associated with a channel group that the event is associated with then use it
				}
			}

			return sendto;
		}
		
		
	}
}
