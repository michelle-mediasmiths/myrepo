package com.mediasmiths.foxtel.ip.mail.templater;

import com.mediasmiths.foxtel.ip.common.email.Emailaddress;
import com.mediasmiths.foxtel.ip.common.email.Emailaddresses;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

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
	protected static Emailaddresses filterByGroups(final List<String> channelGroups, final Emailaddresses original){
				
		log.debug("Event channel groups: "+ StringUtils.join(channelGroups, ','));
		
		List<Emailaddress> emails = original.getEmailaddress();
		Emailaddresses sendto = new Emailaddresses();

		boolean hasAO = channelGroups.contains("AO");
		
		log.debug("Has ao group: "+ hasAO);
		
		for (Emailaddress email : emails)
		{
			log.debug(String.format("Email {%s} group {%s}", email.getValue(), email.getChannelGroup()));
			
			if (StringUtils.isEmpty(email.getChannelGroup()))
			{ // if email address is not associated with a group then always use it, unless the AO group is present
				
				if(!hasAO)
				{
					log.debug("adding " + email.getValue());
					sendto.getEmailaddress().add(email);
				}
				else
				{
					log.debug("not sending ao related email to " + email.getValue());
				}
			}
			else if (channelGroups.contains(email.getChannelGroup()))
			{
				log.debug("adding "+email.getValue());
				sendto.getEmailaddress().add(email); // if email is associated with a channel group that the event is associated with then use it
			}
		}

		return sendto;
	}
}
