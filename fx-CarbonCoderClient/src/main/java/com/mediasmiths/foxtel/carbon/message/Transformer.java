package com.mediasmiths.foxtel.carbon.message;

import org.apache.log4j.Logger;



/**
 * Takes an xml message for carbon and translates into string to be sent down socket connections with carbon
 * @author bmcleod
 *
 */
public class Transformer
{
	private static final String MESSAGE_PREFIX = "CarbonAPIXML1";
	private static final Logger log = Logger.getLogger(Transformer.class);
	
	public String buildMessageForData(String data)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(MESSAGE_PREFIX);
		sb.append(' ');
		sb.append(data.length());
		sb.append(' ');
		sb.append(data);
		
		return sb.toString();
	}

	public String getMessageFromData(String reply)
	{
		String cutPrefix = reply.substring(MESSAGE_PREFIX.length() + 1);
		long length = Long.parseLong(cutPrefix.substring(0, cutPrefix.indexOf(' ')));
		String cutLength = cutPrefix.substring(cutPrefix.indexOf(' ')+1);
		
		if(length != cutLength.length()){
			throw new IllegalArgumentException("Supplied message from carbon did not have a valid length");
		}
		
		return cutLength;		
	}

}
